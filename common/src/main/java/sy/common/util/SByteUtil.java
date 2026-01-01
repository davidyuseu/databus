package sy.common.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.StringUtil;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

@Log4j2
public class SByteUtil
{
	private static final Logger LOGGER = LoggerFactory.getLogger(SByteUtil.class);

	/*
	 * 由先低后高的byte数组得到short类型
	 */
	public static short getShort(byte[] bytes)
	{
		return (short) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
	}

	/*
	 * 由先低后高的byte数组得到int类型
	 */
	public static int getInt(byte[] bytes)
	{
		return (0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)) | (0xff0000 & (bytes[2] << 16))
				| (0xff000000 & (bytes[3] << 24));
	}

	/*
	 * 由先低后高的byte数组得到int类型
	 */
	public static long getLong(byte[] bytes)
	{
		return (0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)) | (0xff0000 & (bytes[2] << 16))
				| (0xff000000 & (bytes[3] << 24));
	}

	/*
	 * 将byte字节型数据转换为0~255（0xFF 即无符号byte）
	 */
	public static int getUnsignedByte(byte data)
	{
		return (data & 0x0FF);
	}

	/*
	 * 将short转为无符号，用int返回（0xFFFF即WORD，范围：0~65535）
	 */
	public static int getUnsignedShort(short data)
	{
		return data & 0x0FFFF;
	}

	/*
	 * 将int转为无符号，用long返回（0xFFFFFFFF即DWORD，范围：0~4294967295）
	 */
	public static long getUnsignedInt(int data)
	{
		return Integer.toUnsignedLong(data);
	}

	/*
	 * 将一个byte或两个byte字节型数据转换为0~255（0xFF 即无符号byte）
	 * 或将两个byte字节的数据转换为0~65535（0xFFFF 即无符号byte） 如0x90
	 * ，本质上：先转为int => 0x00000090;
	 */
	public static int getUnsignedByte(byte[] data)
	{
		if (data.length == 1)
			return data[0] & 0x0FF;
		if (data.length == 2)
		{
			short t = getShort(data);
			return t & 0x0FFFF;
		}
		throw new RuntimeException("The length of the byte array must be 1 or 2!");
	}

	/*
	 * 将int转为低字节在前，高字节在后的byte数组
	 */
	public static byte[] intToByteArrayLH(int n)
	{
		byte[] b = new byte[4];
		b[0] = (byte) (n & 0xff);
		b[1] = (byte) (n >> 8 & 0xff);
		b[2] = (byte) (n >> 16 & 0xff);
		b[3] = (byte) (n >> 24 & 0xff);
		return b;
	}

	/*
	 * 将int转为高字节在前，低字节在后的byte数组
	 */
	public static byte[] intToByteArrayHL(int n)
	{
		byte[] b = new byte[4];
		b[3] = (byte) (n & 0xff);
		b[2] = (byte) (n >> 8 & 0xff);
		b[1] = (byte) (n >> 16 & 0xff);
		b[0] = (byte) (n >> 24 & 0xff);
		return b;
	}


	/**
	 * 将short转为低字节在前，高字节在后的byte数组
	 * @param n short
	 * @return byte[]
	 */
	public static byte[] shortToBytesLH(short n) {
		byte[] b = new byte[2];
		b[0] = (byte) (n & 0xff);
		b[1] = (byte) (n >> 8 & 0xff);
		return b;
	}

	/*
	 * 求byte[]数组累加和
	 */
	public static int sumBytes(byte[] bt)
	{
		int sum = 0;
		for(byte b:bt)
		{
			sum+=b;
		}
		
		return sum;
	}


	/*
	 * 截取指定索引之间(包含start，end)的byte数组并返回
	 */
	public static byte[] cutBytes(byte[] srcBytes, int start, int end)
	{
		byte[] b = new byte[end - start + 1];
		for (int i = 0; i < end - start + 1; i++)
		{
			b[i] = srcBytes[start + i];
		}
		return b;
	}

	/*
	 * 将byte数组转int
	 */
	@Deprecated
	public static Integer charsToInt(byte[] bs)
	{
		String str = new String(bs);
		return Integer.valueOf(str);
	}

	/*
	 * 在byte数组中找到第一个指定byte元素时，返回该索引值 若返回值为-1，则说明未找到该元素
	 */
	public static Integer byteIndexOf(byte[] srcBytes, byte b)
	{
		int index = -1;
		for (int i = 0; i < srcBytes.length; i++)
		{
			if (srcBytes[i] == b)
			{
				index = i;
				break;
			}
		}
		return index;
	}

	/*
	 * 在byte数组指定的下标区间中找到第一个指定byte元素时，返回该索引值 若返回值为-1，则说明未找到该元素
	 */
	public static Integer byteIndexOf(byte[] srcBytes, byte b, int start, int end)
	{
		int index = -1;
		for (int i = start; i <= end; i++)
		{
			if (srcBytes[i] == b)
			{
				index = i;
				break;
			}
		}
		return index;
	}

	/*
	 * 截取字符串中指定的两个字符之间的部分，不包含strStart和strEnd
	 */
	public static String subStringA_B(String str, String strStart, String strEnd)
	{
		int st = str.indexOf(strStart);
		int ed = str.indexOf(strEnd);
		if (st < 0 || ed < 0)// 字符串str中搜不到strStart或strEnd
		{
			return null;
		}
		String result = str.substring(st + 1, ed);
		return result;
	}

	//设置某字节的某bit为1
	public static byte setBitTo1(byte originByte, int bitIndex){
		return originByte |= (1 << bitIndex);
	}

	//设置某字节的某bit为0
	public static byte setBitTo0(byte originByte, int bitIndex){
		return originByte &= ~(1 << bitIndex);
	}

	//获取某字节的某bit值
	public static int getBitValue(byte b,int bitIndex){
		return ((b)>>(bitIndex)&1);
	}

	/*
	 * 将一个int值，按起始bit和终点bit放进一个byte中
	 */
	public static byte putIntIntoByteByBit(int intValue,byte b, int bitStart, int bitEnd ){
		if(bitEnd>7||bitStart>7){
			LOGGER.error("{}","bit的位置大于D7!");
			return Byte.parseByte(null);
		}
		if(bitStart>bitEnd)
		{
			LOGGER.error("{}","bit的起始位大于了结束位！");
			return Byte.parseByte(null);
		}
		if(Math.pow(2,bitEnd-bitStart+1)<intValue){
			LOGGER.error("{}","该int值超出了指定的bit区间能表示的上限！");
			return Byte.parseByte(null);
		}

		int len = bitEnd-bitStart+1;
		byte[] intByte = intToByteArrayLH(intValue);
		for(int i =0;i<len;i++){
			int bitValue = getBitValue(intByte[0],i);
			if(bitValue==0){
				b = setBitTo0(b,bitStart+i);
			}else{
				b = setBitTo1(b,bitStart+i);
			}
		}
		return b;
	}

	/**
	 * @Description: translate BCDBytes to Long
	 * @param pData the bytes of BCD
	 * @param len validate length in the bytes
	 * @return the value of BCD in Long
	 */
	public static long BCDToLong(byte[] pData, int len)
	{
		String tempStr = "";
		String c = "";
		long result = 0;
		for (int i = len - 1; i >= 0; i--)
		{
			c = String.format("%1$02x", pData[i]);
			tempStr = tempStr + c;
		}
		try
		{
			result = Long.parseLong(tempStr);
		} catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public static String asciiToString(byte[] bytes){
		StringBuilder strBuilder = new StringBuilder();
		for(byte b : bytes){
			strBuilder.append((char)b);
		}
		return strBuilder.toString();
	}

	public static String asciiToString(byte[] bytes, int offset, int len){
		if(offset + len > bytes.length)
			return null;
		if(offset < 0 || len < 0)
			return null;
		StringBuilder strBuilder = new StringBuilder();
		int i = 0;
		while(i < len){
			strBuilder.append((char)bytes[offset + i]);
			i++;
		}
		return strBuilder.toString();
	}

	//Retrieves the native byte order of the underlying platform.
	private final static ByteOrder byteOrder = ByteOrder.nativeOrder();

	//trans long and bytes
	public static byte[] longToBytes(long val, ByteOrder order){
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.order(order);
		buffer.putLong(val);
		return buffer.array();
	}

	public static byte[] longToBytes(long val){
		return longToBytes(val, byteOrder);
	}

	public static long bytesToLong(byte[] bytes, ByteOrder order){
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.order(order);
		buffer.put(bytes);
		buffer.flip();
		return buffer.getLong();
	}

	public static long bytesToLong(byte[] bytes){
		return bytesToLong(bytes, byteOrder);
	}

	//trans float and bytes
	public static byte[] floatToBytes(float val, ByteOrder order){
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(order);
		buffer.putFloat(val);
		return buffer.array();
	}

	public static byte[] floatToBytes(float val){
		return floatToBytes(val, byteOrder);
	}

	public static float bytesToFloat(byte[] bytes, ByteOrder order){
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(order);
		buffer.put(bytes);
		buffer.flip();
		return buffer.getFloat();
	}

	public static float bytesToFloat(byte[] bytes){
		return bytesToFloat(bytes, byteOrder);
	}

	//trans double and bytes
	public static byte[] doubleToBytes(double val, ByteOrder order){
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.order(order);
		buffer.putDouble(val);
		return buffer.array();
	}

	public static byte[] doubleToBytes(float val){
		return doubleToBytes(val, byteOrder);
	}

	public static double bytesToDouble(byte[] bytes, ByteOrder order){
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.order(order);
		buffer.put(bytes);
		buffer.flip();
		return buffer.getDouble();
	}

	public static double bytesToDouble(byte[] bytes){
		return bytesToDouble(bytes, byteOrder);
	}

	//trans short and bytes
	public static byte[] shortToBytes(short val, ByteOrder order){
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.order(order);
		buffer.putShort(val);
		return buffer.array();
	}

	public static byte[] shortToBytes(short val){return shortToBytes(val, byteOrder);}

	public static short bytesToShort(byte[] bytes, ByteOrder order){
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.order(order);
		buffer.put(bytes);
		buffer.flip();
		return buffer.getShort();
	}

	public static short bytesToShort(byte[] bytes){
		return bytesToShort(bytes, byteOrder);
	}

	//trans int and bytes
	public static byte[] intToBytes(int val, ByteOrder order){
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(order);
		buffer.putInt(val);
		return buffer.array();
	}

	public static byte[] intToBytes(int val){return intToBytes(val, byteOrder);}

	public static int bytesToInt(byte[] bytes, ByteOrder order){
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(order);
		buffer.put(bytes);
		buffer.flip();
		return buffer.getInt();
	}

	public static int bytesToInt(byte[] bytes){
		return bytesToInt(bytes, byteOrder);
	}

	/** -DataBus------------------------------------------------------------------------------------------------------*/

	/** -ByteBuf------------------------------------------------------------------------------------------------------*/
	public static String logByteBuf(ByteBuf buffer) {
		int length = buffer.readableBytes();
		int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
		StringBuilder buf = new StringBuilder(rows * 80 * 2)
				.append("read index:").append(buffer.readerIndex())
				.append(" write index:").append(buffer.writerIndex())
				.append(" capacity:").append(buffer.capacity())
				.append(NEWLINE);
		appendPrettyHexDump(buf, buffer);
		return buf.toString();
	}

	/**
	 * 将{@param frameBuf}中从索引为{@param startPos}开始，长度为{@param len}的数组，以无符号方式录入long型中
	 * */
	public static long getULongBytes(int byteOrder, ByteBuf frameBuf, int startPos, int len) {
		if (len > 8)
			throw new RuntimeException("The array length exceeded 8, could not translate into long type!");

		switch (len) {
			case 1 :
				return frameBuf.getUnsignedByte(startPos);
			case 2 :
				return byteOrder == 0
						? frameBuf.getUnsignedShortLE(startPos)
						: frameBuf.getUnsignedShort(startPos);
			case 3 :
				return byteOrder == 0
						? frameBuf.getUnsignedMediumLE(startPos)
						: frameBuf.getUnsignedMedium(startPos);
			case 4 :
				return byteOrder == 0
						? frameBuf.getUnsignedIntLE(startPos)
						: frameBuf.getUnsignedInt(startPos);
			case 8 :
				return byteOrder == 0
						? frameBuf.getLongLE(startPos)
						: frameBuf.getLong(startPos);
			default :
				byte[] bs = new byte[8];
				if (byteOrder == 0) {
					frameBuf.getBytes(startPos, bs, 0, len);
					return getLongLE(bs, 0);
				} else {
					frameBuf.getBytes(startPos, bs, 8 - len, len);
					return getLong(bs, 0);
				}
		}
	}

	public static long getLong(byte[] memory, int index) {
		return  ((long) memory[index]     & 0xff) << 56 |
				((long) memory[index + 1] & 0xff) << 48 |
				((long) memory[index + 2] & 0xff) << 40 |
				((long) memory[index + 3] & 0xff) << 32 |
				((long) memory[index + 4] & 0xff) << 24 |
				((long) memory[index + 5] & 0xff) << 16 |
				((long) memory[index + 6] & 0xff) <<  8 |
				(long) memory[index + 7] & 0xff;
	}

	public static long getLongLE(byte[] memory, int index) {
		return  (long) memory[index]      & 0xff        |
				((long) memory[index + 1] & 0xff) <<  8 |
				((long) memory[index + 2] & 0xff) << 16 |
				((long) memory[index + 3] & 0xff) << 24 |
				((long) memory[index + 4] & 0xff) << 32 |
				((long) memory[index + 5] & 0xff) << 40 |
				((long) memory[index + 6] & 0xff) << 48 |
				((long) memory[index + 7] & 0xff) << 56;
	}

	public static long getULongBits(int byteOrder, ByteBuf frameBuf, int startPos, int len,
									int bitStart, int bitLen) {
		long var = getULongBytes(byteOrder, frameBuf, startPos, len);
		var <<= (63 - bitStart);
		var >>>= (63 - bitLen + 1);
		return var;
	}


	/**
	 * 将{@param frameBuf}中从索引为{@param startPos}开始，长度为{@param len}的数组，以有符号方式录入long型中
	 * such as :
	 * startPos = 2;
	 * len = 5;
	 * frameBuf:
	 *          +-------------------------------------------------+
	 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
	 * +--------+-------------------------------------------------+
	 * |00000000|  00 00 80 80 54 90 eb 00 00 00                  |
	 * +--------+-------------------------------------------------+
	 *                   ↑           ↑
	 *  byteOrder = 1 (big endian):
	 *   step1:
	 *   buf.getBytes => byte[8]
	 *   +--------------------------+
	 *   |  0  1  2  3  4  5  6  7  |
	 *   +--------------------------+
	 *   |  00 00 00 80 80 54 90 eb |
	 *   +--------------------------+
	 *
	 *   step2: translate into big endian long value
	 *   getLong(byte[8])  => 0x80805490EB
	 *
	 *   step3: 0x00000080805490eb << offset >> offset
	 *          => 0xFFFFFF80805490EB (-547602788117)
	 *
	 *  byteOrder = 0 (little endian)
	 *   step1:
	 *   buf.getBytes => byte[8]
	 *   +--------------------------+
	 *   |  0  1  2  3  4  5  6  7  |
	 *   +--------------------------+
	 *   |  80 80 54 90 eb 00 00 00 |
	 *   +--------------------------+
	 *
	 *   step2: translate into little endian long value
	 *   getLongLE(byte[8])  => 0xEB90548080
	 *
	 *   step3: 0x000000EB90548080 << offset >> offset
	 *          => 0xFFFFFFEB90548080 (-87772856192)
	 *
	 * */
	public static long getLongBytes(int byteOrder, ByteBuf frameBuf, int startPos, int len) {
		if (len > 8)
			throw new RuntimeException("The array length exceeded 8, could not translate into long type!");

		switch (len) {
			case 1 :
				return frameBuf.getByte(startPos);
			case 2 :
				return byteOrder == 0
						? frameBuf.getShortLE(startPos)
						: frameBuf.getShort(startPos);
			case 3 :
				return byteOrder == 0
						? frameBuf.getMediumLE(startPos)
						: frameBuf.getMedium(startPos);
			case 4 :
				return byteOrder == 0
						? frameBuf.getIntLE(startPos)
						: frameBuf.getInt(startPos);
			case 8 :
				return byteOrder == 0
						? frameBuf.getLongLE(startPos)
						: frameBuf.getLong(startPos);
			default :
				byte[] bs = new byte[8];
				long var;
				if (byteOrder == 0) {
					frameBuf.getBytes(startPos, bs, 0, len);
					var = getLongLE(bs, 0);

				} else {
					frameBuf.getBytes(startPos, bs, 8 - len, len);
					var = getLong(bs, 0);
				}
				int offset = 8 * (8 - len);
				return var << offset >> offset;
		}
	}

	/**
	 * BCD码转为10进制串(阿拉伯数据)
	 * @param bytes : BCD码
	 * @return : 10进制串
	 */
	public static String bcd2Str(byte[] bytes){
		StringBuilder temp = new StringBuilder(bytes.length * 2);
		for (int i = bytes.length - 1; i >= 0; --i) {
			temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
			temp.append((byte) (bytes[i] & 0x0f));
		}

		return temp.toString();
	}

	/**
	 * Input format:
	 *  √ "eb900401"
	 *  √ "eb 90 04 01"  (preEditHexInputString方法会去除空白字符)
	 *      => byte[]{0xeb, 0x90, 0x04, 0x01}
	 *
	 *  × "0x90eb" 不识别"0x"
	 *  × "eb,90" 或 "eb，90" 或 "eb;90" 不识别非[a-zA-Z0-9]的字符
	 *  × "eb90e" 长度不为偶数
	 *
	 * */
	public static ByteBuf getBufByString(String input) throws Exception{
		if(!input.isEmpty()) {
			input = SStringUtil.preEditHexInputString(input);
			byte[] bytes = StringUtil.decodeHexDump(input);
			if (bytes.length > 0) {
				return Unpooled.wrappedBuffer(bytes);
			} else {
				log.warn("{}","The length of byte array transferred by input is 0!");
				return null;
			}
		}else{
			log.warn("{}","The input String is empty!");
			return null;
		}
	}

	/**
	 * 返回从haystack中找到needle时的索引
	 */
	public static int indexOf(ByteBuf haystack, ByteBuf needle) {
		return indexOf(haystack, needle, haystack.readerIndex());
	}

	public static int indexOf(ByteBuf haystack, ByteBuf needle, int startPos) {
		for (int i = startPos; i < haystack.writerIndex(); i ++) {
			int haystackIndex = i;
			int needleIndex;
			for (needleIndex = 0; needleIndex < needle.capacity(); needleIndex ++) {
				if (haystack.getByte(haystackIndex) != needle.getByte(needleIndex)) {
					break;
				} else {
					haystackIndex ++;
					if (haystackIndex == haystack.writerIndex() &&
							needleIndex != needle.capacity() - 1) {
						return -1;
					}
				}
			}

			if (needleIndex == needle.capacity()) {
				// Found the needle from the haystack
				return i; // return i - haystack.readerIndex();
			}
		}
		return -1;
	}

	/**
	 * 清理MappedByteBuffer
	 * */
	private static Object invoke(final Object target, final String methodName, final Class<?>... args) {
		return AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
			try {
				Method method = method(target, methodName, args);
				method.setAccessible(true);
				return method.invoke(target);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		});
	}

	private static Method method(Object target, String methodName, Class<?>[] args) throws NoSuchMethodException {
		try {
			return target.getClass().getMethod(methodName, args);
		} catch (NoSuchMethodException e) {
			return target.getClass().getDeclaredMethod(methodName, args);
		}
	}

	private static ByteBuffer viewed(ByteBuffer buffer) {
		String methodName = "viewedBuffer";
		Method[] methods = buffer.getClass().getMethods();
		for (Method method : methods) {
			if (method.getName().equals("attachment")) {
				methodName = "attachment";
				break;
			}
		}

		ByteBuffer viewedBuffer = (ByteBuffer)  invoke(buffer, methodName);
		if (viewedBuffer == null)
			return buffer;
		else
			return viewed(viewedBuffer);
	}

	/**
	 * ps: 被clean后的buffer引用请手动置为null
	 * */
	public static void clean(final ByteBuffer buffer) {
		if (buffer == null || !buffer.isDirect() || buffer.capacity() == 0)
			return;
		invoke(invoke(viewed(buffer), "cleaner"), "clean");
	}

	public static void releaseBufArray(ByteBuf[] bufs) {
		if (bufs != null && bufs.length > 0) {
			for (int i = 0; i < bufs.length; i++) {
				bufs[i].release();
				bufs[i] = null;
			}
		}
	}

	public static void releaseAndClearBufs(List<ByteBuf> bufs) {
		if (bufs != null) {
			for (var buf : bufs) {
				buf.release();
			}
			bufs.clear();
		}
	}
}
