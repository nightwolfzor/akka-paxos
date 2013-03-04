package com.dragisak.paxos.multi

import akka.actor._

class Scout(
  val acceptors: Set[ActorRef],
  val b: Ballot
) extends Actor with ActorLogging{

  var waitFor = acceptors
  var pValues = Set[PValue]()


  override def preStart = acceptors.foreach(_ ! Phase1a(self, b))


  def receive = {
    case Phase1b(a, b1, r) =>
      if (b1 == b) {
        pValues = pValues ++ r
        waitFor = waitFor - a

        if (waitFor.size < acceptors.size /2) {
          context.parent ! Adopted(b, pValues)
          self ! PoisonPill
        }
      } else {
        context.parent ! Preempted(b1)
        self ! PoisonPill
      }

  }
}
