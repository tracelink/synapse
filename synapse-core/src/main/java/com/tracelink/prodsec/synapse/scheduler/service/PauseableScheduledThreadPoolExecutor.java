package com.tracelink.prodsec.synapse.scheduler.service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tracelink.prodsec.synapse.encryption.service.DataEncryptionService;

class PauseableScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor{
	private static final Logger LOG = LoggerFactory.getLogger(PauseableScheduledThreadPoolExecutor.class);

	PauseableScheduledThreadPoolExecutor(int threadCount) {
		super(threadCount);
		setRemoveOnCancelPolicy(true);
	}

	private volatile boolean isPaused;

	private final ReentrantLock pauseLock = new ReentrantLock();

	private final Condition unpaused = pauseLock.newCondition();
	private int additionalTasksPaused = 0;

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		additionalTasksPaused++;
		pauseLock.lock();
		try {
			while (isPaused) {
				unpaused.await();
			}
		} catch (InterruptedException ie) {
			t.interrupt();
		} finally {
			pauseLock.unlock();
		}
		additionalTasksPaused--;
	}

	void pause() {
		pauseLock.lock();
		try {
			isPaused = true;
			LOG.warn("ThreadPool has been PAUSED");
		} finally {
			pauseLock.unlock();
		}
	}

	boolean isPaused() {
		return this.isPaused;
	}

	void resume() {
		pauseLock.lock();
		try {
			isPaused = false;
			unpaused.signalAll();
			LOG.warn("ThreadPool has been RESUMED");
		} finally {
			pauseLock.unlock();
		}
	}

	@Override
	public long getTaskCount() {
		return super.getTaskCount() + additionalTasksPaused;
	}
}
