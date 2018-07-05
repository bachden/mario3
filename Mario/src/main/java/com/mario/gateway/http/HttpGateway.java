package com.mario.gateway.http;

import java.io.IOException;
import java.util.Map.Entry;

import javax.servlet.AsyncContext;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.mario.config.gateway.GatewayType;
import com.mario.config.gateway.HttpGatewayConfig;
import com.mario.entity.message.Message;
import com.mario.entity.message.MessageRW;
import com.mario.entity.message.impl.HttpMessage;
import com.mario.entity.message.transcoder.MessageDecodingException;
import com.mario.gateway.AbstractGateway;
import com.mario.gateway.serverwrapper.HasServerWrapper;
import com.mario.worker.MessageEventFactory;
import com.mario.worker.MessageHandlingWorkerPool;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuNull;
import com.nhb.common.data.PuValue;
import com.nhb.common.utils.PrimitiveTypeUtils;

public class HttpGateway extends AbstractGateway<HttpGatewayConfig>
		implements HasServerWrapper<JettyHttpServerWrapper> {
	private JettyHttpServerWrapper serverWrapper;

	private ServletHolder holder = new ServletHolder(new HttpServlet() {

		private static final long serialVersionUID = 1L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			if (req.getRequestURI().endsWith("favicon.ico")) {
				return;
			}
			HttpGateway.this.handle(req, resp);
		}

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			if (req.getRequestURI().endsWith("favicon.ico")) {
				return;
			}
			HttpGateway.this.handle(req, resp);
		}

		@Override
		protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			if (req.getRequestURI().endsWith("favicon.ico")) {
				return;
			}
			HttpGateway.this.handle(req, resp);
		}

		@Override
		protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			if (req.getRequestURI().endsWith("favicon.ico")) {
				return;
			}
			HttpGateway.this.handle(req, resp);
		}

		@Override
		protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			HttpGateway.this.handle(req, resp);
		}

		@Override
		protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			HttpGateway.this.handle(req, resp);
		}

		@Override
		protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			HttpGateway.this.handle(req, resp);
		}
	});

	protected MessageHandlingWorkerPool createWorkerPool() {
		MessageHandlingWorkerPool messageHandlingWorkerPool = new MessageHandlingWorkerPool();
		if (this.getConfig().getWorkerPoolConfig() == null) {
			getLogger().info("Http gateway " + this.getName() + ", configured in " + this.getExtensionName()
					+ " has an empty worker pool config, using sync mode as default");
		} else {
			messageHandlingWorkerPool.setConfig(this.getConfig().getWorkerPoolConfig());
		}
		return messageHandlingWorkerPool;
	}

	@Override
	public void setServer(JettyHttpServerWrapper serverWrapper) {
		this.serverWrapper = serverWrapper;
	}

	@Override
	protected void _init() {
		if (this.getConfig().getWorkerPoolConfig() == null) {
			this.getConfig().setAsync(false);
		}
		if (this.getConfig().isUseMultipath()) {
			getLogger().debug("Http gateway " + this.getName() + " using multipath...");
			this.holder.getRegistration().setMultipartConfig(new MultipartConfigElement(this.getConfig().getPath()));
		}
		this.serverWrapper.addServlet(holder, this.getConfig().getPath());
	}

	private void handle(final HttpServletRequest request, final HttpServletResponse response) {
		final Message message;
		try {
			if (getConfig().isAsync()) {
				message = this.publishToWorkers(request.startAsync());
			} else {
				HttpMessage httpMessage = new HttpMessage();

				httpMessage.setGatewayName(getName());
				httpMessage.setRequest(request);
				httpMessage.setResponse(response);
				getDeserializer().decode(request, (MessageRW) httpMessage);

				message = httpMessage;
				PuElement puResponse = this.getHandler().handle(message);
				if (puResponse == PuNull.IGNORE_ME) {
					getLogger().warn("PuNull response cannot be used in sync mode, sending EMPTY value as response");
					puResponse = PuValue.fromObject("");
				}
				this.onHandleComplete(message, puResponse);
			}
		} catch (MessageDecodingException e) {
			this.onHandleError(e.getTarget(), e.getCause());
		}
	}

	@Override
	protected void _start() throws Exception {
		if (!this.serverWrapper.isRunning()) {
			this.serverWrapper.start();
		}
		getLogger().info("Http gateway listening on location: " + this.getConfig().getPath() + ", server: "
				+ this.getConfig().getServerWrapperName() + ", worker pool: " + this.getConfig().getWorkerPoolConfig());
	}

	@Override
	protected void _stop() throws Exception {
		if (this.serverWrapper != null && this.serverWrapper.isRunning()) {
			this.serverWrapper.stop();
		}
	}

	private void closeAsyncContext(Message message) {
		AsyncContext asyncContext = ((HttpMessage) message).getContext();
		if (asyncContext != null) {
			try {
				asyncContext.complete();
			} catch (Exception e) {
				// getLogger().error("Error while closing async context", e);
			}
		}
	}

	private HttpServletResponse extractResponse(Message message) {
		if (message instanceof HttpMessage) {
			return (HttpServletResponse) ((HttpMessage) message).getResponse();
		}
		return null;
	}

	private void writeResponseAndDone(final Message message, final Object data) {
		try {
			HttpServletResponse responser = extractResponse(message);
			if (responser != null) {
				Object res = null;
				if (data != null) {
					if (this.getSerializer() != null) {
						try {
							res = this.getSerializer().encode(data);
						} catch (Exception e) {
							try {
								res = this.getSerializer().encode(e);
							} catch (Exception e1) {
								getLogger().error("Cannot serialize response data", e1);
								res = "Cannot serialize response data";
							}
						}
					} else {
						res = data;
					}

					if (res == null) {
						res = "";
					}

					try {
						if (this.getConfig().getHeaders().size() > 0) {
							for (Entry<String, String> header : this.getConfig().getHeaders().entrySet()) {
								responser.setHeader(header.getKey(), header.getValue());
							}
						}
						if (this.getConfig().getContentType() != null) {
							responser.setContentType(this.getConfig().getContentType());
						}
						if (this.getConfig().getEncoding() != null) {
							responser.setCharacterEncoding(this.getConfig().getEncoding());
						}

						if (res instanceof String) {
							responser.getWriter().write((String) res);
						} else if (res instanceof PuElement) {
							responser.getWriter().write(((PuElement) res).toJSON());
						} else if (res instanceof Throwable) {
							responser.getWriter().write(ExceptionUtils.getStackTrace((Throwable) res));
						} else if (PrimitiveTypeUtils.isPrimitiveOrWrapperType(res.getClass())) {
							responser.getWriter().write(PrimitiveTypeUtils.getStringValueFrom(res));
						} else {
							responser.getWriter().write(res.toString());
						}

						responser.getWriter().flush();

					} catch (IOException e) {
						getLogger().error("Cannot write response", e);
					}
				} else {
					try {
						responser.getWriter().flush();
					} catch (IOException e) {
						getLogger().error("Error while writing response", e);
					}
				}
			}
		} finally {
			closeAsyncContext(message);
		}
	}

	@Override
	public void onHandleComplete(Message message, PuElement result) {
		if (result instanceof PuNull) {
			return;
		}

		writeResponseAndDone(message, result);
	}

	@Override
	public void onHandleError(Message message, Throwable exception) {

		getLogger().error(
				"Error while handling message " + (message.getData() == null ? "" : (", data: " + message.getData())),
				exception);

		HttpServletResponse response = extractResponse(message);
		if (response != null) {
			if (message instanceof HttpMessage) {
				response.setStatus(((HttpMessage) message).getHttpStatus());
			} else {
				response.setStatus(500);
			}
		} else {
			getLogger().error("", new NullPointerException("response is null, please check"));
		}
		writeResponseAndDone(message, exception);
	}

	@Override
	protected MessageEventFactory createEventFactory() {
		return new MessageEventFactory() {
			@Override
			public Message newInstance() {
				HttpMessage msg = new HttpMessage();
				msg.setGatewayName(getName());
				msg.setCallback(HttpGateway.this);
				msg.setGatewayType(GatewayType.HTTP);
				return msg;
			}
		};
	}

	public SessionHandler getSessionManager() {
		return serverWrapper.getSessionHandler();
	}

}
