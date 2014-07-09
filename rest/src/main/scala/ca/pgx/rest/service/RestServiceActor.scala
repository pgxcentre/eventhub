package ca.pgx.rest.service

import akka.actor.{Actor, ActorLogging}

trait RestServiceActor extends Actor with RestService with ActorLogging {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(route)
}

class SimpleRestServiceActor extends RestServiceActor // with WhateverMixIns added later
