package gash.grpc.route.server;

import com.google.protobuf.ByteString;

import io.grpc.stub.StreamObserver;
import route.Route;

public class ServerWork implements Runnable {

	private Route request;
	private StreamObserver<Route> responseObserver;


	public ServerWork(Route request, StreamObserver<route.Route> responseObserver) {
		this.request = request;
		this.responseObserver = responseObserver;
	}

	@Override
	public void run() {
		
		  
/////// to check if the payload received from Client is Odd or Even and returns the result
		
		ByteString pl_byteStr= this.request.getPayload();
		String payload = new String(pl_byteStr.toByteArray());
		String return_msg = "";
		int i =Integer.parseInt(payload);
		System.out.println("+++++++++++++++++ PAYLOAD" + payload +"PAYLOAD++++++++++++++++++++++");
		if(i % 2 == 0) {
			return_msg = "Even Number";
		} else {
			return_msg = "Odd Number";
		}
		
		Route response = Route.newBuilder(this.request)
				.setId(RouteServer.getInstance().getNextMessageID())
				.setOrigin(RouteServer.getInstance().getServerID())
				.setDestination(request.getOrigin())
				.setMessage(return_msg)
				.setPath(String.format("%s->%s", this.request.getPath(),RouteServer.getInstance().getServerName()))
				.setPayload(process())
				.build();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		responseObserver.onNext(response);
		responseObserver.onCompleted();

	}

	private ByteString process() {

// To process
		final String serv_name = String.format("Number from  %s", RouteServer.getInstance().getServerName());
		byte[] byte_arr = serv_name.getBytes();
		return ByteString.copyFrom(byte_arr);
	}

}
