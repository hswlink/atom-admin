package com.atom.plugins.disruptor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import com.atom.plugins.mongo.kit.MongoKit;
import com.atom.plugins.mongo.kit.MongoQuery;
import com.jfinal.plugin.IPlugin;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.IgnoreExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.WorkerPool;
import com.lmax.disruptor.dsl.ProducerType;

public class DisruptorPlugin implements IPlugin {
	public static RingBuffer<LogEvent> buffer;
	public static  WorkerPool<LogEvent> workerPool;
	public static  ExecutorService executors;
	public boolean start() {
	  	  EventFactory<LogEvent> eventFactory=new EventFactory<LogEvent>() {  
	          public LogEvent newInstance() {  
	              return new LogEvent();  
	          }  
	      };  
		    buffer = RingBuffer.create(ProducerType.MULTI,         //定义一个ringBuffer,也就是相当于一个队列  
		    		eventFactory, 1 << 2, new BlockingWaitStrategy());  
		    // 定义消费者,只要有生产出来的东西，该事件就会被触发,参数event 为被生产出来的东西　  
		    //几个workerHandler 表示有几个消费者  
		    WorkHandler<LogEvent> workHandler = new WorkHandler<LogEvent>() {  
		        @Override  
		        public void onEvent(LogEvent event) throws Exception {  
		        	MongoQuery query = new MongoQuery();
		        	query.use("atom").set(event.getLog()).save();
		            System.out.println(Thread.currentThread().getName()+" "+ReflectionToStringBuilder.toString(event));  
		        }  
		    };  
		      
		      
		    //定义一个消费者池，每一个handler都是一个消费者，也就是一个线程，会不停地就队列中请求位置，如果这们位置中被生产者放入了东西，而这个东西则是上面定义  
		    //RingBuffer中的 factory 创建出来的一个event,消费者会取出这个event,调用handler中的onEvent方法，如果这个位置还没有被生产者放入东西，则阻塞，等待生产者  
		    //生产后唤醒.  
		    //而生产者在生产时要先申请slot，而这个slot位置不能高于最后一个消费者的位置，否则会覆盖没有消费的slot，如果大于消费者的最后一个slot，则进行自旋等待.  
		    workerPool = new WorkerPool<LogEvent>(buffer,  
		            buffer.newBarrier(), new IgnoreExceptionHandler(), workHandler,workHandler,workHandler,workHandler);  
		    //每个消费者，也就是 workProcessor都有一个sequence，表示上一个消费的位置,这个在初始化时都是-1  
		    Sequence[] sequences = workerPool.getWorkerSequences();   
		    //将其保存在ringBuffer中的 sequencer 中，在为生产申请slot时要用到,也就是在为生产者申请slot时不能大于此数组中的最小值,否则产生覆盖  
		    buffer.addGatingSequences(sequences);  
		    executors = Executors.newFixedThreadPool(10);
		    workerPool.start(executors);         //用executor 来启动 workProcessor 线程  
		    System.out.println("disruptor started "); 		
		    
		return true;
	}

	public boolean stop() {
		 workerPool.halt();   
		 executors.shutdown();
		return true;
	}

}
