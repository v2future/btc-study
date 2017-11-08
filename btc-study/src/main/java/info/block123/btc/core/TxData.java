package info.block123.btc.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 交易原始数据
 * @author v2future
 *
 */
public class TxData {
	 // The offset is how many bytes into the provided byte array this message starts at.
    protected transient int offset;
    // The raw message bytes themselves.
    protected transient byte[] bytes;
    protected transient int length = Integer.MIN_VALUE;
    protected transient byte[] checksum;
    
    public TxData () {}
    public TxData (byte[] msg, int offset) {
    	this.bytes = msg;
    	this.offset = offset;
    }
    
    /**
     * 返回有效数据副本
     */
    public byte[] bitcoinSerialize() {
        byte[] bytes = unsafeBitcoinSerialize();
        byte[] copy = new byte[bytes.length];
        System.arraycopy(bytes, 0, copy, 0, bytes.length);
        return copy;
    }
    
    public byte[] unsafeBitcoinSerialize() {
        // 1st attempt to use a cached array.
        if (bytes != null) {
            if (offset == 0 && length == bytes.length) {
                return bytes;
            }
            byte[] buf = new byte[length];
            System.arraycopy(bytes, offset, buf, 0, length);
            return buf;
        }
        //序列化后的逻辑未处理 TODO
        return null;
    }
}
