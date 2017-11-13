package info.block123.btc.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import info.block123.btc.kit.BtcKit;

/**
 * 区块信息
 * 	大小			字段			描述
	4字节			版本			版本号，用于跟踪软件/协议的更新
	32字节		父区块哈希值	引用区块链中父区块的哈希值
	32字节		Merkle根		该区块中交易的merkle树根的哈希值
	4字节			时间戳		该区块产生的近似时间（精确到秒的Unix时间戳）
	4字节			难度目标		该区块工作量证明算法的难度目标
	4字节			Nonce		用于工作量证明算法的计数器
 * @author v2future
 *
 */
public class Block {
	
	public static final int HEADER_SIZE = 80;
	/**区块大小**/
	public static final int MAX_BLOCK_SIZE = 1 * 1000 * 1000;
	public static final int MAX_BLOCK_SIGOPS = MAX_BLOCK_SIZE / 50;
	 
	private long version;
    private Sha256Hash prevBlockHash;
    private Sha256Hash merkleRoot;
    private long time;
    private long nBits; // "nBits"
    private long nonce;
    List<Transaction> transactions;
    //raw data
    protected transient int offset;
    // The raw message bytes themselves.
    protected transient byte[] bytes;

    private transient Sha256Hash hash;
    
    public Block() {
    	version = 1;
    	nBits = 0x1d07fff8L;
        time = System.currentTimeMillis() / 1000;
        prevBlockHash = Sha256Hash.ZERO_HASH;
    }
    
    /**
     * 一个区块给予的奖励
     * @param height
     * @return
     */
    public BigInteger getBlockInflation(int height) {
        return null;
    }
    
    /**
     * 区块hash
     * @return
     */
    public Sha256Hash getHash() {
        if (hash == null)
            hash = calculateHash();
        return hash;
    }
    
    /**
     * 返回morkle根节点
     * @return
     */
    public Sha256Hash getMerkleRoot() {
        if (merkleRoot == null) {
            merkleRoot = calculateMerkleRoot();
        }
        return merkleRoot;
    }
    
    private Sha256Hash calculateHash() {
        try {
            ByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(HEADER_SIZE);
            writeHeader(bos);
            return new Sha256Hash(BtcKit.sha256hash160(bos.toByteArray()));
        } catch (IOException e) { 
            throw new RuntimeException(e); // Cannot happen.
        }
    }
    
    private void writeHeader(OutputStream stream) throws IOException {
        // try for cached write first
        if (bytes != null && bytes.length >= offset + HEADER_SIZE) {
            stream.write(bytes, offset, HEADER_SIZE);
            return;
        }
        // fall back to manual write
        //maybeParseHeader();
        BtcKit.uint32ToByteStreamLE(version, stream);
        stream.write(BtcKit.reverseBytes(prevBlockHash.getBytes()));
        stream.write(BtcKit.reverseBytes(getMerkleRoot().getBytes()));
        BtcKit.uint32ToByteStreamLE(time, stream);
        BtcKit.uint32ToByteStreamLE(nBits, stream);
        BtcKit.uint32ToByteStreamLE(nonce, stream);
    }

    
    private Sha256Hash calculateMerkleRoot() {
        List<byte[]> tree = buildMerkleTree();
        return new Sha256Hash(tree.get(tree.size() - 1));
    }

    private List<byte[]> buildMerkleTree() {
        // The Merkle root is based on a tree of hashes calculated from the transactions:
        //
        //     root
        //      / \
        //   A      B
        //  / \    / \
        // t1 t2 t3 t4
        //
        // The tree is represented as a list: t1,t2,t3,t4,A,B,root where each
        // entry is a hash.
        //
        // The hashing algorithm is double SHA-256. The leaves are a hash of the serialized contents of the transaction.
        // The interior nodes are hashes of the concenation of the two child hashes.
        //
        // This structure allows the creation of proof that a transaction was included into a block without having to
        // provide the full block contents. Instead, you can provide only a Merkle branch. For example to prove tx2 was
        // in a block you can just provide tx2, the hash(tx1) and B. Now the other party has everything they need to
        // derive the root, which can be checked against the block header. These proofs aren't used right now but
        // will be helpful later when we want to download partial block contents.
        //
        // Note that if the number of transactions is not even the last tx is repeated to make it so (see
        // tx3 above). A tree with 5 transactions would look like this:
        //
        //         root
        //        /     \
        //       1        5
        //     /   \     / \
        //    2     3    4  4
        //  / \   / \   / \
        // t1 t2 t3 t4 t5 t5
        //maybeParseTransactions(); TODO
        ArrayList<byte[]> tree = new ArrayList<byte[]>();
        // Start by adding all the hashes of the transactions as leaves of the tree.
        for (Transaction t : transactions) {
            tree.add(t.getHash().getBytes());
        }
        int levelOffset = 0; // Offset in the list where the currently processed level starts.
        // Step through each level, stopping when we reach the root (levelSize == 1).
        for (int levelSize = transactions.size(); levelSize > 1; levelSize = (levelSize + 1) / 2) {
            // For each pair of nodes on that level:
            for (int left = 0; left < levelSize; left += 2) {
                // The right hand node can be the same as the left hand, in the case where we don't have enough
                // transactions.
                int right = Math.min(left + 1, levelSize - 1);
                byte[] leftBytes = BtcKit.reverseBytes(tree.get(levelOffset + left));
                byte[] rightBytes = BtcKit.reverseBytes(tree.get(levelOffset + right));
                //TODO
                //tree.add(BtcKit.reverseBytes(doubleDigestTwoBuffers(leftBytes, 0, 32, rightBytes, 0, 32)));
            }
            // Move to the next level.
            levelOffset += levelSize;
        }
        return tree;
    }
    
}
