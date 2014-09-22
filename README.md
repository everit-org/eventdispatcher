eventdispatcher
===============

Utility classes to be able to write an event dispatcher that sends
historical "replay" event data to newly registered listeners as well.

The programmer must define four types to be able to use this library:

 - Type of the listener
 - Key of the listener that identifies the listener. The motivation here
   is that e.g. in OSGi service objects can be identified by their service
   references so one service object may be used multiple times. In this
   case the service reference identifies the listener while the listener
   object itself is the registered service object.
 - Type of the event
 - Key of the event

In case several events are in a chain the programmer must define the object
that identifies the chain. The defined object is the key of the event. When
a new event is dispatched in the chain the previous will be removed so only
the last event in the chain will be replayed in case of a new listener
registration.

When the four types are defined the programmer can instantiate
EventDispatcherImpl. See the JavaDoc of that class about constructor
parameters.

[![Analytics](https://ga-beacon.appspot.com/UA-15041869-4/everit-org/eventdispatcher)](https://github.com/igrigorik/ga-beacon)
