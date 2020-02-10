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


package org.wannagoframework.audit.service;

import java.time.Instant;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wannagoframework.audit.domain.Session;
import org.wannagoframework.audit.exception.ServiceException;
import org.wannagoframework.audit.repository.SessionRepository;
import org.wannagoframework.commons.utils.HasLogger;

/**
 * @author WannaGo Dev1.
 * @version 1.0
 * @since 2019-05-24
 */
@Transactional(readOnly = true)
@Service
public class SessionServiceImpl implements SessionService, HasLogger {

  private final SessionRepository sessionRepository;

  public SessionServiceImpl(SessionRepository sessionRepository) {
    this.sessionRepository = sessionRepository;
  }

  @Override
  public Page<Session> findAnyMatching(String filter, Pageable pageable) {
    String loggerString = getLoggerPrefix("findAnyMatching");
    logger().debug(loggerString + "In = " + filter);
    Page<Session> result;

    if (StringUtils.isNotBlank(filter)) {
      result = sessionRepository.findByCriteria(filter, pageable);
    } else {
      result = sessionRepository.findAll(pageable);
    }

    logger().debug(loggerString + "Out = " + result);

    return result;
  }


  @Override
  public long countAnyMatching(String filter) {
    String loggerString = getLoggerPrefix("countAnyMatching");
    logger().debug(loggerString + "In = " + filter);
    long result;
    if (StringUtils.isNotBlank(filter)) {
      result = sessionRepository.countByCriteria(filter);
    } else {
      result = sessionRepository.count();
    }

    logger().debug(loggerString + "Out = " + result);
    return result;
  }

  @Transactional
  @Override
  public Session login(String jsessionId, String sourceIp, String username, Boolean isSuccess,
      String error) throws ServiceException {
    String loggerPrefix = getLoggerPrefix("login");
    Optional<Session> _session = sessionRepository.getByJsessionId(jsessionId);
    Session session;
    if (_session.isPresent()) {
      session = _session.get();
      if (!session.getUsername().equals(username)) {
        logger().error(loggerPrefix + "SESSION ID '" + jsessionId
            + "' is already associated with another user '" + session.getUsername()
            + "', expecting '" + username + "'");
        throw new ServiceException("SESSION ID is already associated with another user");
      }
      return session;
    } else {
      session = new Session();
      session.setJsessionId(jsessionId);
      session.setSourceIp(sourceIp);
      session.setUsername(username);
      session.setIsSuccess(isSuccess);
      session.setError(error);
      session.setSessionStart(Instant.now());
      return sessionRepository.save(session);
    }
  }

  @Transactional
  @Override
  public void logout(String jsessionId) {
    String loggerPrefix = getLoggerPrefix("logout");
    Optional<Session> _session = sessionRepository.getByJsessionId(jsessionId);
    if (_session.isPresent()) {
      Session session = _session.get();
      session.setSessionEnd(Instant.now());
      session.setSessionDuration(
          session.getSessionEnd().toEpochMilli() - session.getSessionStart().toEpochMilli());
      sessionRepository.save(session);
    } else {
      logger().error(
          String.format("%sSession with jsessionId '%s' not found", loggerPrefix, jsessionId));
    }
  }

  @Override
  public MongoRepository<Session, String> getRepository() {
    return sessionRepository;
  }
}
