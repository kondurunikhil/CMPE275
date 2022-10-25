package gash.grpc.route.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

import gash.grpc.route.client.RouteClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import route.HeartBeat;
import route.Route;
import route.RouteServiceGrpc;
import route.RouteServiceGrpc.RouteServiceImplBase;

/**
 * copyright 2021, gash
 *
 * Gash licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
public class RouteServerImpl extends RouteServiceImplBase {

	private static Logger logger = LoggerFactory.getLogger(RouteServerImpl.class);

	
	private final int Queue_Size = 1;
	private LinkedBlockingQueue<ServerWork> queue = new LinkedBlockingQueue<ServerWork>(Queue_Size);
	private Server curr_svr;

	/**
	 * Configuration of the server's identity, port, and role
	 */
	private static Properties getConfiguration(final File path) throws IOException {
		if (!path.exists())
			throw new IOException("missing file");

		Properties rtn = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(path);
			rtn.load(fis);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}

		return rtn;
	}
	
	
	private static final HeartBeat constructMessage(String alive) {
		
		HeartBeat.Builder bld = HeartBeat.newBuilder();
		bld.setLife(alive);
		
		return bld.build();
    }

	/**
	 * TODO refactor this!
	 * 
	 * @param path
	 * @param payload
	 * @return
	 */
	public static void main(String[] args) throws Exception {
		
		System.out.println("Server Args +++++++++++" + args[0]);
		// checking if configuration file is available
		if (args.length < 1) {
			logger.info("Require serevr conf argument to proceed");
		}
		
		String path = args[0];
		try {
			Properties conf = RouteServerImpl.getConfiguration(new File(path));
			RouteServer.configure(conf);
			// printing server name
			logger.info(RouteServer.getInstance().getServerName() + "--------Server Name-------");
			
//			if(RouteServer.getInstance().getServerRole().equalsIgnoreCase("heartbeat")) {
//				System.out.println("    +++++++++      " + RouteServer.getInstance().getServerRole());
//			
//			} else {
//				
//				ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 4327).usePlaintext().build();
//				RouteServiceGrpc.RouteServiceBlockingStub stub = RouteServiceGrpc.newBlockingStub(channel);
//				
//				var msg = RouteServerImpl.constructMessage("alive");
//				var r = stub.heartbeat(msg);
//				
//			}
			/* Similar to the socket, waiting for a connection */
			final RouteServerImpl impl = new RouteServerImpl();
			impl.start();
			impl.blockUntilShutdown();

		} catch (IOException e) {
			// TODO better error message
			e.printStackTrace();
		}
	}

	private void start() throws Exception {
		curr_svr = ServerBuilder.forPort(RouteServer.getInstance().getServerPort()).addService(new RouteServerImpl())
				.build();

		logger.info("-- starting server");		
	
		curr_svr.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				RouteServerImpl.this.stop();
			}
		});
	}

	protected void stop() {
		curr_svr.shutdown();
	}

	private void blockUntilShutdown() throws Exception {
		/* TODO what clean up is required? */
		curr_svr.awaitTermination();
	}
	
	
	/**
	 * server received a message!
	 */
	@Override
	public void request(Route request, StreamObserver<Route> responseObserver) {

		logger.info("Request Received from Server "+  RouteServer.getInstance().getServerName()
		+" with payaload as "+ new String(request.getPayload().toByteArray()) +" from Origin " +request.getOrigin());
			
		try {
				ServerWork work = new ServerWork(request, responseObserver);
				queue.add(work);
				logger.info(" +++++++++++++ Work assigned for!" + request.getOrigin() + "++++++++++++++++++++++");
				logger.info("+++++++++++++ cureent size of queue is : "+queue.size() + "++++++++++++++++++++++++");
				
		
		} catch (IllegalStateException e) {
			// forward
			logger.info( "NO SPACE AVAILABLE IN QUEUE");
			Long ServerId= RouteServer.getInstance().getNextServerID();
			if ( ServerId != 9000L) {
				Route res = null;
				logger.info("----Forward to : "+RouteServer.getInstance().getNextServerID() +"--" + request.getOrigin());
				ManagedChannel com_chnl = assignServer();
				RouteServiceGrpc.RouteServiceBlockingStub stub = RouteServiceGrpc.newBlockingStub(com_chnl);
				
				// Configuring new path to direct the work to next available server
				try {
					Route nextReq = Route.newBuilder(request).setPath(
							String.format("%s->%s", request.getPath(), RouteServer.getInstance().getServerName()))
							.build();
					res = stub.request(nextReq);
					responseObserver.onNext(res);
					responseObserver.onCompleted();
				} catch (StatusRuntimeException se) {
					if (se.getStatus().getCode() == Status.Code.UNAVAILABLE) {
						logger.info( " +++++++++++++ The current server is unavailable ++++++++++");
						//checkNextServers(request,responseObserver,3);
						logger.info(se.getMessage());
						responseObserver.onError(se); // return the error back to previous client
					} else {
						logger.error("Error while generating request to new Server");
						se.printStackTrace();
					}
				}
				com_chnl.shutdown();
			} 
			
		}

		// resting the thread
		try {
			Thread.sleep(3500);
		} catch (InterruptedException exep) {
			exep.printStackTrace();
		}
		
		// Implementing the assigned work
		while (!queue.isEmpty()) {
		
			ServerWork Swork;
			try {
				Swork = queue.take();
				// to find the given payload is Odd or Even
				Swork.run();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

	}
	
	
	private ManagedChannel assignServer() {
		ManagedChannel channel = null;
		try {
			channel = ManagedChannelBuilder
					.forAddress("localhost", RouteServer.getInstance().getNextServerPort())
					.usePlaintext()
					.build();
			 
			logger.info(" ------------------------- Serve Assigned Sucesfully -------------------------        ");
			
		}
		catch (StatusRuntimeException se) {
			logger.error("+++++++++++++++++++++++++ Error While Assigning SERVER +++++++++++++++++++++++++");
			
		}
		
		return channel;
	}
	
//
//

}
