package com.google.reddcoin.examples;

import java.io.File;
import java.math.BigInteger;

import com.google.reddcoin.core.AbstractPeerEventListener;
import com.google.reddcoin.core.Address;
import com.google.reddcoin.core.Message;
import com.google.reddcoin.core.Peer;
import com.google.reddcoin.core.Transaction;
import com.google.reddcoin.core.Utils;
import com.google.reddcoin.kits.WalletAppKit;
import com.google.reddcoin.params.RegTestParams;
import com.google.reddcoin.utils.BriefLogFormatter;
import com.google.reddcoin.utils.Threading;
import com.google.reddcoin.core.Wallet;

/**
 * This is a little test app that waits for a coin on a local regtest node, then  generates two transactions that double
 * spend the same output and sends them. It's useful for testing double spend codepaths but is otherwise not something
 * you would normally want to do.
 */
public class DoubleSpend {
    public static void main(String[] args) throws Exception {
        BriefLogFormatter.init();
        final RegTestParams params = RegTestParams.get();
        WalletAppKit kit = new WalletAppKit(params, new File("."), "doublespend");
        kit.connectToLocalHost();
        kit.setAutoSave(false);
        kit.startAsync();
        kit.awaitRunning();

        System.out.println(kit.wallet());

        kit.wallet().getBalanceFuture(Utils.COIN, Wallet.BalanceType.AVAILABLE).get();
        Transaction tx1 = kit.wallet().createSend(new Address(params, "muYPFNCv7KQEG2ZLM7Z3y96kJnNyXJ53wm"), Utils.CENT);
        Transaction tx2 = kit.wallet().createSend(new Address(params, "muYPFNCv7KQEG2ZLM7Z3y96kJnNyXJ53wm"), Utils.CENT.add(BigInteger.TEN));
        final Peer peer = kit.peerGroup().getConnectedPeers().get(0);
        peer.addEventListener(new AbstractPeerEventListener() {
            @Override
            public Message onPreMessageReceived(Peer peer, Message m) {
                System.err.println("Got a message!" + m.getClass().getSimpleName() + ": " + m);
                return m;
            }
        }, Threading.SAME_THREAD);
        peer.sendMessage(tx1);
        peer.sendMessage(tx2);

        Thread.sleep(5000);
        kit.stopAsync();
        kit.awaitTerminated();
    }
}
