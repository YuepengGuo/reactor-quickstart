/**
 * @author Stephane Maldini
 */


import reactor.event.selector.Selectors
import groovy.transform.CompileStatic

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import reactor.core.Environment
import reactor.core.spec.Reactors
import reactor.quickstart.Trade
import reactor.quickstart.TradeServer

@CompileStatic
void test() {
	CountDownLatch latch
	int totalTrades = 10000000
	long startTime
	long endTime
	double elapsed
	double throughput

	def startTimer = {
		println "Starting throughput test with $totalTrades trades..."
		latch = new CountDownLatch(totalTrades)
		startTime = System.currentTimeMillis()
	}

	def endTimer = {
		latch.await(30, TimeUnit.SECONDS);
		endTime = System.currentTimeMillis()
		elapsed = (endTime - startTime) * 1.0
		throughput = totalTrades / (elapsed / 1000)
		println "Executed ${(int) throughput} trades/sec in $elapsed ms"
	}

	def env = new Environment()
	def server = new TradeServer()

	// Use a Reactor to dispatch events using the RingBuffer Dispatcher
	def reactor = Reactors.reactor().env(env).dispatcher(Environment.RING_BUFFER).get()

	def topic = 'trade.execute'

	// For each Trade event, execute that on the server
	reactor.on(Selectors.object(topic)) { Trade trade ->
		server.execute trade

		// Since we're async, for this test, use a latch to tell when we're done
		latch.countDown()
	}

	// Start a throughput timer
	startTimer()

	// Publish one event per trade
	for (int i in 0..totalTrades) {

		// Notify the Reactor the next randomly-generated Trade from server is ready to be handled
		reactor.notify topic, server.nextTrade()
	}

	// Stop throughput timer and output metrics
	endTimer()

	server.stop()
}

test()


