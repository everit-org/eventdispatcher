eventdispatcher
===============

Utility classes to be able to write an event dispatcher that sends
historical "replay" event data to newly registered listeners as well.

In case a listener throws an exception or does not give a response
in a timeout it will be blacklisted and no event will be dispatched
to that listener in the future.

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

For more information see the [maven generated site][1].

[1]: http://everit.org/mvnsites/eventdispatcher

Motivation
----------

This library was created during implementing the Blueprint OSGi Enterprise
5.0 specification. In Blueprint there is an event dispatching mechanism
described that could be used by other technologies as well. The library
itself does not contains any blueprint specific code, it will be used from
the new Blueprint implementation.

Roadmap
-------

After using the library from other technologies without bug report, 1.0.0
will be released. The library itself is atomic enough not to have many
new features in the future. Till 1.0.0 more documents will be created and
a bit more possibility to handle timeouts.
