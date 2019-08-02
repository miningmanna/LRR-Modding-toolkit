package de.mm.lrrmod.flh;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;

public class FLHFile {
	
	public int width;
	public int height;
	private int lframes;
	public ArrayList<BufferedImage> frames;
	
	public static FLHFile getFLHFileFromImages(ArrayList<BufferedImage> imgs) {
		
		if(imgs == null || imgs.size() == 0)
			return null;
		
		FLHFile file = new FLHFile();
		
		file.frames = imgs;
		file.lframes = imgs.size();
		BufferedImage img1 = imgs.get(0);
		file.width = img1.getWidth();
		file.height = img1.getHeight();
		
		return file;
	}
	
	public static FLHFile getFLHFile(InputStream in) throws IOException {
		
		FLHFile flh = new FLHFile();
		
		byte[] header = new byte[128];
		in.read(header);
		
		int offset = 0;
		int flength = getIntLE(header, offset);
		offset += 4;
		short type = getShortLE(header, offset);
		offset += 2;
		if(type != (short) 0xAF43)
			System.out.println("WARNING: is not a LRR FLH file!");
		
		flh.lframes = getShortLE(header, offset);
		offset += 2;
		flh.frames = new ArrayList<>(flh.lframes);
		flh.width = getShortLE(header, offset);
		offset += 2;
		flh.height = getShortLE(header, offset);
		offset += 2;
		short depth = getShortLE(header, offset);
		if(depth != 16)
			System.out.println("Expected 16 bit colors");
		offset += 68; // Unimportend data
		
		int offset1stFrame = getIntLE(header, offset);
		offset += 4;
		
		offset = offset1stFrame;
		
		byte[] segHeader = new byte[6];
		int imageIndex = 0;
		while((flength - offset) > 0) {
			in.read(segHeader);
			int segLen = -6 + getIntLE(segHeader, 0);
			byte[] seg = new byte[segLen];
			in.read(seg);
			offset += segLen + 6;
			short segType = getShortLE(segHeader, 4);
			switch (segType) {
			case (short) 0xF1FB:
				System.out.println("Segment table present!");
				break;
			case (short) 0xF1FA:
				imageIndex += parseFRAME_TYPE(flh, seg, imageIndex);
				break;
			default:
				System.out.println("Unknown chunk type: " + Integer.toHexString(segType));
				break;
			}
		}
		
		in.close();
		
		return flh;
	}
	
	private static int parseFRAME_TYPE(FLHFile flh, byte[] seg, int imageIndex) {
		int offset = 0;
		short lchunks = getShortLE(seg, offset);
		if(lchunks > 1)
			System.out.println("More than one sub-chunk");
		offset += 2;
		offset += 2;
		offset += 2; // reserved = 0
		offset += 4; // width and height should be 0
		
		if(seg.length < 16) {
			flh.lframes -= 1;
			return 0;
		}
		
		int subLen = getIntLE(seg, offset);
		subLen -= 6;
		offset += 4;
		short chunkType = getShortLE(seg, offset);
		offset += 2;
		
		switch (chunkType) {
		case 25:
			parseDTA_BRUN(flh, seg, offset, subLen, imageIndex);
			break;
		case 27:
			parseDTA_LC(flh, seg, offset, subLen, imageIndex);
			break;
		default:
			System.out.println("Unsupported sub-chunk type: " + chunkType);
			break;
		}
		return 1;
	}
	
	private static void parseDTA_BRUN(FLHFile flh, byte[] seg, int offset, int len, int imageIndex) {
		
		BufferedImage res = new BufferedImage(flh.width, flh.height, BufferedImage.TYPE_INT_ARGB);
		
		int x = 0;
		int y = 0;
		int w = flh.width;
		
		offset += 1;
		
		while((len-offset) > 0) {
			byte repeat = seg[offset];
			if(repeat < 0) {
				repeat = (byte) (repeat * -1);
				for(int i = 0; i < repeat; i++) {
					int rgb = getARGBFrom555RGB(seg, offset+i*2+1);
					
					res.setRGB(x, y, rgb);
					x++;
				}
				offset += repeat*2+1;
			} else {
				
				int rgb = getARGBFrom555RGB(seg, offset+1);
				
				for(int i = 0; i < repeat; i++) {
					res.setRGB(x, y, rgb);
					x++;
				}
				offset += 3;
			}
			
			if(x >= w) {
				x %= w;
				y++;
				if(y > flh.height)
					break;
				offset++;
			}
			
		}
		for(; y < flh.height; y++) {
			for(; x < w; x++) {
				res.setRGB(x, y, 0xFF000000);
			}
			x = 0;
		}
		
		flh.frames.add(imageIndex, res);
		
	}
	
	private static void parseDTA_LC(FLHFile flh, byte[] seg, int offset, int len, int imageIndex) {
		BufferedImage res = new BufferedImage(flh.width, flh.height, BufferedImage.TYPE_INT_ARGB);
		if(imageIndex != 0)
			res.getGraphics().drawImage(flh.frames.get(imageIndex-1), 0, 0, null);
		
		short llines = getShortLE(seg, offset);
		offset += 2;
		
		int y = 0;
		int linesDone = 0;
		while((len - offset) > 0) {
			
			if(llines == linesDone) {
				System.out.println("Line already done. Unexpected data: " + (len-offset));
				break;
			}
			
			int packCount = -1;
			while(packCount == -1) {
				short opcode = getShortLE(seg, offset);
				offset += 2;
				int optype = (0x0000C000 & opcode) >> 14;
				switch (optype) {
				case 0:
					packCount = opcode;
					break;
				case 2:
					System.out.println("Last Pixel?");
					break;
				case 3:
					y += Math.abs(opcode) & 0x000000FF;
					break;
				default:
					System.out.println("Unknown opcode: " + opcode);
					break;
				}
			}
			
			int x = 0;
			for(int i = 0; i < packCount; i++) {
				x += 0x000000FF & seg[offset];
				offset++;
				byte repeat = seg[offset];
				offset++;
				if(repeat < 0) {
					repeat = (byte) (-1*repeat);
					
					int rgb = getARGBFrom555RGB(seg, offset);
					for(int j = 0; j < repeat; j++) {
						res.setRGB(x, y, rgb);
						x++;
					}
					
					offset += 2;
				} else {
					for(int j = 0; j < repeat; j++) {
						int rgb = getARGBFrom555RGB(seg, offset+j*2);
						res.setRGB(x, y, rgb);
						x++;
					}
					offset += repeat*2;
					
				}
			}
			y++;
			
//			if(hasLastPixel)
//				offset--;
			
			linesDone++;
			
		}
		
		
		flh.frames.add(imageIndex, res);
	}
	
	private static int getARGBFrom555RGB(byte[] a, int offset) {
		
		int rgb = 0x000000FF;
		rgb &= a[offset+1];
		rgb = rgb << 8;
		rgb |= 0x000000FF & a[offset];
		
		int r = (int) ((rgb >> 10) * (255.0f/31.0f));
		int g = (int) (((rgb >> 5) & 0b00011111) * (255.0f/31.0f));
		int b = (int) ((rgb & 0b00011111) * (255.0f/31.0f));
		
		rgb = 0x0000FF00 | r;
		rgb = rgb << 8;
		rgb |= g;
		rgb = rgb << 8;
		rgb |= b;
		
		return rgb;
	}
	
	private static short getShortLE(byte[] b, int off) {
		
		int res = 0;
		for(int i = 0; i < 2; i++) {
			res = res | (0x000000FF & b[off+1-i]);
			if(i != 1)
				res = res << 8;
		}
		
		return (short) res;
		
	}
	
	private static int getIntLE(byte[] b, int off) {
		
		int res = 0;
		for(int i = 0; i < 4; i++) {
			res = res | (0x000000FF & b[off+3-i]);
			if(i != 3)
				res = res << 8;
		}
		
		return res;
		
	}
	
	public static void writeFlhFile(FLHFile flh, OutputStream out) throws IOException {
		int size = 128;
		byte[] header = new byte[128];
		writeLEShort(0xAF43, header, 4);
		writeLEShort(flh.frames.size(), header, 6);
		writeLEShort(flh.width, header, 8);
		writeLEShort(flh.height, header, 10);
		writeLEShort(16, header, 12);
		header[0x50] = (byte) 0x80; // Magic???
		LinkedList<byte[]> chunks = new LinkedList<>();
		for(int i = 0; i < flh.frames.size(); i++) {
			byte[] frame = makeFRAME_TYPE(flh, i, chunks);
			size += frame.length;
			chunks.add(frame);
		}
		System.out.println("Made FRAMES");
		writeLEInt(size, header, 0);
		out.write(header);
		out.flush();
		int lChunks = chunks.size();
		for(int i = 0; i < lChunks; i++) {
			byte[] chunk = chunks.pop();
			out.write(chunk);
		}
	}
	
	private static byte[] makeFRAME_TYPE(FLHFile flh, int imgIndex, LinkedList<byte[]> chunks) throws IOException {
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		buffer.write(new byte[4]); // DUMMY SIZE
		buffer.write(new byte[] {(byte) 0xFA, (byte) 0xF1}); // Lower endian 0xF1FA FRAME_TYPE code
		buffer.write(0x01);
		buffer.write(0);    // ONE SUBCHUNK
		buffer.write(new byte[8]); // EMPTY
		
		// always only 1 subchunk.
		// image 0 is DTA_BRUN
		// All other DELTA_FLC
		
		byte[] subChunk = null;
		subChunk = null;
		if(imgIndex == 0) {
			// DTA_BRUN
			subChunk = makeDTA_BRUN(flh, imgIndex);
		} else {
			// DELTA_FLC
//			System.out.println("DTA_LC");
//			subChunk = makeDTA_LC(flh, imgIndex);
			subChunk = makeDTA_BRUN(flh, imgIndex);
		}
		int len = 16 + subChunk.length;
		buffer.write(subChunk);
		byte[] res = buffer.toByteArray();
		writeLEInt(len, res, 0);
		
		return res;
	}
	
	private static byte[] makeDTA_BRUN(FLHFile flh, int imgIndex) throws IOException {
		
		byte[] res = null;
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		buffer.write(new byte[4]); // CHUNK SIZE
		buffer.write(0x19); // CHUNK TYPE 0x0019 (LE) DTA_BRUN
		buffer.write(0);
		buffer.write(0); // MYTERIOUS UNUSED BYTE?
		
		int w = flh.width;
		BufferedImage origImg = flh.frames.get(imgIndex);
		BufferedImage img = new BufferedImage(origImg.getWidth(), origImg.getHeight(), BufferedImage.TYPE_USHORT_555_RGB);
		img.getGraphics().drawImage(origImg, 0, 0, null);
		short[] imgData = ((DataBufferUShort)img.getRaster().getDataBuffer()).getData();
		for(int y = 0; y < flh.height; y++) {
			int x = 0;
			while(x < w) {
				if(w-x < 3) {
					buffer.write((byte) (x-w));
					for(int i = 0; i < w-x; i++) {
						int rgb = imgData[y*w+x];
						buffer.write(rgb & 0x00FF);
						buffer.write(rgb>>8);
					}
					x = w;
				} else {
					boolean repeat = true;
					int orig = imgData[y*w+x];
					for(int i = 1; i < 3; i++)
						if(imgData[y*w+x] != orig)
							repeat = false;
					if(repeat) {
						int o = 0;
						while(imgData[y*w+x+o] == orig && o < 127) {
							o++;
							if((x+o) >= w)
								break;
						}
						buffer.write((byte) o);
						buffer.write(orig & 0x00FF);
						buffer.write(orig>>8);
						x += o;
					} else {
						int o = 2;
						while(!(	imgData[y*w+x+o] == imgData[y*w+x+o-1]
								&& 	imgData[y*w+x+o-1] == imgData[y*w+x+o-2])
								&& o < 128 && (x+o) < w) {
							o++;
							if((x+o) >= w)
								break;
						}
						buffer.write((byte) (o*-1));
						for(int i = 0; i < o; i++) {
							int rgb = imgData[y*w+x+i];
							buffer.write(rgb & 0x00FF);
							buffer.write(rgb>>8);
						}
						x += o;
					}
				}
			}
			buffer.write(0); // MYTERIOUS UNUSED BYTE?
		}
		
		res = buffer.toByteArray();
		writeLEInt(res.length, res, 0);
		return res;
	}
	
//	private static byte[] makeDTA_LC(FLHFile flh, int imgIndex) throws IOException {
//		
//		byte[] res = null;
//		
//		BufferedImage origImg1 = flh.frames.get(imgIndex-1);
//		BufferedImage img1 = new BufferedImage(origImg1.getWidth(), origImg1.getHeight(), BufferedImage.TYPE_USHORT_555_RGB);
//		img1.getGraphics().drawImage(origImg1, 0, 0, null);
//		short[] imgData1 = ((DataBufferUShort)img1.getRaster().getDataBuffer()).getData();
//		BufferedImage origImg2 = flh.frames.get(imgIndex);
//		BufferedImage img2 = new BufferedImage(origImg2.getWidth(), origImg2.getHeight(), BufferedImage.TYPE_USHORT_555_RGB);
//		img2.getGraphics().drawImage(origImg2, 0, 0, null);
//		short[] imgData2 = ((DataBufferUShort)img2.getRaster().getDataBuffer()).getData();
//		
//		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//		
//		buffer.write(new byte[4]); // CHUNK SIZE PLACEHOLDER
//		buffer.write(0x1B); // CHUNK TYPE (LE) 0x001B = DTA_LC
//		buffer.write(0);
//		buffer.write(new byte[2]); // NUMBER-OF-LINES PLACEHOLDER
//		
//		LinkedList<byte[]> lines = new LinkedList<>();
//		int w = img1.getWidth(), h = img1.getHeight();
//		int linesToSkip = 0;
//		boolean firstLine = true;
//		for(int y = 0; y < h; y++) {
//			boolean isEqual = true;
//			for(int x = 0; x < img1.getHeight(); x++) {
//				if(imgData2[y*w+x] != imgData1[y*w+x]) {
//					isEqual = false;
//					break;
//				}
//			}
//			if(isEqual) {
//				linesToSkip++;
//				continue;
//			}
//			
//			System.out.println("Skipping lines: " + linesToSkip);
//			
//			ByteArrayOutputStream buff = new ByteArrayOutputStream();
//			
//			// Write skip lines
//			int packCountOffset = 0;
//			while(linesToSkip > 255) {
//				buff.write(0x00FF);
//				buff.write(0b0000000011000000);
//				linesToSkip -= 255;
//				packCountOffset += 2;
//			}
//			if(!firstLine) {
//				buff.write(((short) linesToSkip) * -1);
//				buff.write(((short) linesToSkip) * -1 >> 8);
//				linesToSkip = 0;
//				packCountOffset += 2;
//			} else {
//				firstLine = false;
//			}
//			
//			buff.write(new byte[2]); // PACKET_COUNT PLACEHOLDER
//			int packCount = 0;
//			int columnSkip = 0;
//			int lastx = 0;
//			for(int x = 0; x < w; x++) {
//				
//				if(imgData1[y*w+x] == imgData2[y*w+x]) {
//					columnSkip++;
//					continue;
//				}
//				System.out.println("COLUMN SKIP: " + columnSkip);
//				while(columnSkip > 255) {
//					lastx += 255;
//					buff.write(0xFF);
//					buff.write(0xFF);
//					int rgb = imgData1[y*w+lastx];
//					buff.write(rgb & 0x00FF);
//					buff.write(rgb>>8);
//					columnSkip -= 255;
//				}
//				buff.write(0x00FF & columnSkip);
//				columnSkip = 0;
//				
//				int written = 0;
//				
//				for(int i = 0; i < 255 && x+i < w; i++) {
//					if(imgData1[y*w+x+i] == imgData2[y*w+x+i]) {
//						written = i;
//						break;
//					}
//				}
//				
//				System.out.println("CHANGED: " + written);
//				
//				int ox = x;
//				boolean firstPacket = true;
//				while((x-ox) < written) {
//					if(firstPacket) {
//						firstPacket = false;
//					} else {
//						buff.write(0);
//					}
//					if(w-x < 3) {
//						buff.write((byte) (w-x));
//						for(int i = 0; i < w-x; i++) {
//							int rgb = imgData2[y*w+x];
//							buff.write(rgb & 0x00FF);
//							buff.write(rgb>>8);
//						}
//						x = w;
//					} else {
//						boolean repeat = true;
//						int orig = imgData2[y*w+x];
//						for(int i = 1; i < 3; i++)
//							if(imgData2[y*w+x] != orig)
//								repeat = false;
//						if(repeat) {
//							int o = 0;
//							while(imgData2[y*w+x+o] == orig && o < 127) {
//								System.out.println("REPEAT: " + (x+o) + "/" + w);
//								o++;
//								if((x+o) >= w)
//									break;
//							}
//							buff.write((byte) o * -1);
//							buff.write(orig & 0x00FF);
//							buff.write(orig>>8);
//							x += o;
//							System.out.println("NEXT X: " + x);
//						} else {
//							System.out.println(x);
//							int o = 2;
//							while(!(	imgData2[y*w+x+o] == imgData2[y*w+x+o-1]
//									&& 	imgData2[y*w+x+o-1] == imgData2[y*w+x+o-2])
//									&& o < 128 && (x+o) < w) {
//								System.out.println("NOREPEAT: " + (x+o) + "/" + w);
//								o++;
//								if((x+o) >= w)
//									break;
//							}
//							System.out.println(x+o);
//							buff.write((byte) o);
//							for(int i = 0; i < o; i++) {
//								int rgb = imgData2[y*w+x+i];
//								buff.write(rgb & 0x00FF);
//								buff.write(rgb>>8);
//							}
//							x += o;
//						}
//					}
//					packCount++;
//				}
//				
//				
//				lastx = x;
//			}
//			
//			byte[] lineRes = buff.toByteArray();
//			System.out.println(packCountOffset);
//			System.out.println("LINE LEN: " + lineRes.length);
//			writeLEShort(packCount, lineRes, packCountOffset);
//			System.out.println("PACK COUNT IN LINE: (" + packCount + ")");
//			System.out.println(Integer.toHexString(0x00FF & lineRes[packCountOffset]));
//			System.out.println(Integer.toHexString(0x00FF & lineRes[packCountOffset+1]));
//			
//			lines.add(lineRes);
//		}
//		
//		int _lines = lines.size();
//		for(int i = 0; i < _lines; i++)
//			buffer.write(lines.pop());
//		
//		res = buffer.toByteArray();
//		writeLEInt(res.length, res, 0);
//		writeLEShort(_lines, res, 6);
//		return res;
//		
//	}
	
	private static void writeLEShort(int val, byte[] a, int offset) {
		a[offset]	= (byte) (0x00FF & val);
		a[offset+1]	= (byte) ((0x00FF00 & val)>>8);
	}
	
	private static void writeLEInt(int val, byte[] a, int offset) {
		for(int i = 0; i < 4; i++) {
			a[offset+i] = (byte) (((0x00FF << i*8) & val) >> i*8);
		}
	}
	
}
