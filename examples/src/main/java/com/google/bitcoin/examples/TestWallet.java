import java.io.File;

import com.google.bitcoin.core.AbstractPeerEventListener;
import com.google.bitcoin.core.Block;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.Message;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Peer;
import com.google.bitcoin.kits.WalletAppKit;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.utils.Threading;


public class TestWallet {

	private WalletAppKit appKit;

	public static void main(String[] args) throws Exception {
		new TestWallet().run();
	}

	public void run() throws Exception {
		NetworkParameters params = MainNetParams.get();
		
		appKit = new WalletAppKit(params, new File("."), "dogecoins") {
			@Override
			protected void onSetupCompleted() {
				if (wallet().getKeychainSize() < 1) {
					ECKey key = new ECKey();
					wallet().addKey(key);
				}
				
				peerGroup().setConnectTimeoutMillis(1000);
				
				System.out.println(appKit.wallet());
				
				peerGroup().addEventListener(new AbstractPeerEventListener() {
					@Override
					public void onPeerConnected(Peer peer, int peerCount) {
						super.onPeerConnected(peer, peerCount);
						System.out.println(String.format("onPeerConnected: %s %s",peer,peerCount));
					}
					@Override
					public void onPeerDisconnected(Peer peer, int peerCount) {
						super.onPeerDisconnected(peer, peerCount);
						System.out.println(String.format("onPeerDisconnected: %s %s",peer,peerCount));
					}
					@Override public void onBlocksDownloaded(Peer peer, Block block, int blocksLeft) {
						super.onBlocksDownloaded(peer, block, blocksLeft);
						System.out.println(String.format("%s blocks left (downloaded %s)",blocksLeft,block.getHashAsString()));
					}
					
					@Override public Message onPreMessageReceived(Peer peer, Message m) {
						System.out.println(String.format("%s -> %s",peer,m.getClass()));
						return super.onPreMessageReceived(peer, m);
					}
				},Threading.SAME_THREAD);
			}
		};
		
		appKit.startAndWait();
	}

}
