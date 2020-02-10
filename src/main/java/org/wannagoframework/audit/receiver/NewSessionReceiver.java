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
import org.wannagoframework.audit.exception.ServiceException;
import org.wannagoframework.audit.service.SessionService;
import org.wannagoframework.commons.utils.HasLogger;
import org.wannagoframework.dto.messageQueue.NewSession;

/**
 * @author WannaGo Dev1.
 * @version 1.0
 * @since 2019-07-02
 */
@Component
public class NewSessionReceiver implements HasLogger {

  private final SessionService sessionService;

  public NewSessionReceiver(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  @JmsListener(destination = "newSession")
  public void newSession(Message<NewSession> message) {
    String loggerPrefix = getLoggerPrefix("newSession");
    logger().info(loggerPrefix + "+++++++++++++++++++++++++++++++++++++++++++++++++++++");
    MessageHeaders headers = message.getHeaders();
    logger().info(loggerPrefix + "Headers received : " + headers);

    NewSession newSession = message.getPayload();

    try {
      sessionService
          .login(newSession.getJsessionId(), newSession.getSourceIp(), newSession.getUsername(),
              newSession.getIsSuccess(), newSession.getError());
    } catch (ServiceException e) {
      logger().error(loggerPrefix + "Something wrong happend : " + e.getLocalizedMessage(), e);
    }

    logger().info(loggerPrefix + "Message content received : " + newSession);
    logger().info(loggerPrefix + "+++++++++++++++++++++++++++++++++++++++++++++++++++++");
  }
}
