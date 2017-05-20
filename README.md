# Duct module.ataraxy

[![Build Status](https://travis-ci.org/duct-framework/module.ataraxy.svg?branch=master)](https://travis-ci.org/duct-framework/module.ataraxy)

A [Duct][] module that sets [Ataraxy][] as the router for your
application.

[duct]: https://github.com/duct-framework/duct
[ataraxy]: https://github.com/weavejester/ataraxy

## Installation

To install, add the following to your project `:dependencies`:

    [duct/module.ataraxy "0.1.0-SNAPSHOT"]

## Usage

### Basic

To add this module to your configuration, add the
`:duct.module/ataraxy` key. For example:

```edn
{:duct.core/project-ns foo
 :duct.module/ataraxy  {"/" [:index]}
 :foo.handler/index    {}}
```

The `:duct.module/ataraxy` key should contain a map (or list) of
Ataraxy routes. See the [syntax][] section of Ataraxy's README for
more information on the format it expects.

The module uses the `:duct.core/project-ns` key and the result key to
find an appropriate Integrant key at:

    <project-ns>.handler[.<result key namespace>]/<result key name>

So in the above example, the project namespace is `foo` and the only
result key is `:index`, so the module looks for a `:foo.handler/index`
Integrant key.

If the result key was `:example/index` instead, then the Integrant key
would be `:foo.handler.example/index`.

Similarly, the module looks for middleware at:

    <project-ns>.middleware[.<metadata key namespace>]/<metadata key name>

For example:

```edn
{:duct.core/project-ns   foo
 :duct.module/ataraxy    {"/" ^:example [:index]}
 :foo.handler/index      {}
 :foo.middleware/example {}}
```

[syntax]: https://github.com/weavejester/ataraxy#syntax

### Advanced

If you want more control, you can use the router directly, without the
use of the module. To do this, reference the `:duct.router/ataraxy`
key from `:duct.core/handler`:

```edn
{:duct.core/handler
 {:router #ig/ref :duct.router/ataraxy}
 :duct.router/ataraxy
 {:routes     {"/" ^:example [:index]}
  :handlers   {:index   #ig/ref :foo.handler/index}
  :middleware {:example #ig/ref :foo.middleware/example}
 :foo.handler/index      {}
 :foo.middleware/example {}}
```

You can also start with the module, and override parts as necessary:

```edn
{:duct.core/project-ns foo
 :duct.module/ataraxy  {"/" [:index]}
 :duct.router/ataraxy  {:handlers {:index #ig/ref :foo.handler/custom
 :foo.handler/custom   {}}
```


## License

Copyright © 2017 James Reeves

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
