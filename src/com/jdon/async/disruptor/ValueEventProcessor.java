/*
 * Copyright 2003-2009 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.jdon.async.disruptor;

import java.util.concurrent.TimeUnit;

import com.lmax.disruptor.AlertException;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;

public class ValueEventProcessor {

	protected RingBuffer<ValueEvent> ringBuffer;

	private long waitAtSequence = 0;

	public ValueEventProcessor(RingBuffer<ValueEvent> ringBuffer) {
		this.ringBuffer = ringBuffer;
	}

	public void send(Object result) {
		ringBuffer.setGatingSequences(new Sequence(-1));

		waitAtSequence = ringBuffer.next();
		ValueEvent ve = ringBuffer.get(waitAtSequence);
		ve.setValue(result);
		ringBuffer.publish(waitAtSequence);
	}

	public ValueEvent waitFor(long timeoutforeturnResult) {
		try {
			SequenceBarrier barrier = ringBuffer.newBarrier();
			long a = barrier.waitFor(waitAtSequence, timeoutforeturnResult, TimeUnit.MILLISECONDS);
			return ringBuffer.get(a);
		} catch (AlertException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ValueEvent waitForBlocking() {
		try {
			SequenceBarrier barrier = ringBuffer.newBarrier();
			long a = barrier.waitFor(waitAtSequence);
			ValueEvent ve = ringBuffer.get(a);
			return ve;
		} catch (AlertException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public long getWaitAtSequence() {
		return waitAtSequence;
	}

	public void setWaitAtSequence(long waitAtSequence) {
		this.waitAtSequence = waitAtSequence;
	}

}
