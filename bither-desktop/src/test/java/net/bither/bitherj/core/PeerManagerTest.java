package net.bither.bitherj.core;

import org.junit.Test;

public class PeerManagerTest {

	 @Test 
	    public void testNormal() throws InterruptedException {
	        Block block = new Block(2, "00000000000000000ee9b585e0a707347d7c80f3a905f48fa32d448917335366", "4d60e37c7086096e85c11324d70112e61e74fc38a5c5153587a0271fd22b65c5", 1400928750
	                , 409544770l, 4079278699l, 302400);
	        BlockChain.getInstance().addSPVBlock(block);
	        PeerManager.instance().start();

	        while (true) {
	            Thread.sleep(1000);
	        }
	    }
}
