package info.block123.btc.core;

/**
 * 自定义btc学习项目异常
 * @author v2future
 */ 
public class BtcException extends RuntimeException{

	public BtcException() {
		super();
	}

	public BtcException(String message, Throwable cause) {
		super(message, cause);
	}

	public BtcException(String message) {
		super(message);
	}

	public BtcException(Throwable cause) {
		super(cause);
	}

}
