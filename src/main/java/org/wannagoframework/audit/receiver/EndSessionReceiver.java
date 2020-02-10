/*
 * This file is part of the WannaGo distribution (https://github.com/wannago).
 * Copyright (c) [2019] - [2020].
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */


package org.wannagoframework.audit.receiver;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.wannagoframework.audit.service.SessionService;
import org.wannagoframework.commons.utils.HasLogger;
import org.wannagoframework.dto.messageQueue.EndSession;

/**
 * @author WannaGo Dev1.
 * @version 1.0
 * @since 2019-07-02
 */
@Component
public class EndSessionReceiver implements HasLogger {

  private final SessionService sessionService;

  public EndSessionReceiver(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  @JmsListener(destination = "endSession")
  public void endSession(Message<EndSession> message) {
    String loggerPrefix = getLoggerPrefix("endSession");
    logger().info(loggerPrefix + "+++++++++++++++++++++++++++++++++++++++++++++++++++++");
    MessageHeaders headers = message.getHeaders();
    logger().info(loggerPrefix + "Headers received : " + headers);

    EndSession endSession = message.getPayload();

    sessionService.logout(endSession.getJsessionId());

    logger().info(loggerPrefix + "Message content received : " + endSession);
    logger().info(loggerPrefix + "+++++++++++++++++++++++++++++++++++++++++++++++++++++");
  }
}
