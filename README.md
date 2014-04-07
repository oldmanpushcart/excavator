![logo](https://code.google.com/p/excavator/logo?cct=1342584745) Excavator
=========

excavator（挖掘机）是一个分布式的Java RMI框架。（求项目使用，有兴趣的可以电邮oldmanpushcart@gmail.com）

* [适用场景](http://code.google.com/p/excavator/wiki/ApplicationOfScene)
* [框架设计](http://code.google.com/p/excavator/wiki/FrameworkDesign)
* [快速开始](http://code.google.com/p/excavator/wiki/QuickStart)
* 进阶教程
 * [运行时环境](http://code.google.com/p/excavator/wiki/Runtimes)
 * [通知机制](http://code.google.com/p/excavator/wiki/Advice)
 * [性能监控](http://code.google.com/p/excavator/wiki/MonitorAdvice)
 * [性能诊断](http://code.google.com/p/excavator/wiki/Profiler)
 * 路由规则（1.2版推出）
 * 权重调整（1.2版推出）
 * 服务质量（1.2版推出）
* [性能报告](http://code.google.com/p/excavator/wiki/Performance)
* [更新日志](http://code.google.com/p/excavator/wiki/ChangeLog)(当前最新版本1.1.4)

框架简介
=========
excavator（挖掘机）框架提供了一个网状TOP的分布式架构Java RMI通讯框架。旨在为Java的应用提供一个分布式的服务框架，excavator从分布式应用层面以及统一的发布/调用方式层面为大家提供支持，从而可以很容易的开发分布式的应用以及提供或使用公用功能模块，而不用考虑分布式领域中的各种细节技术，例如远程通讯、性能损耗、调用的透明化、同步调用方式的实现等等问题。

提前Q&A
=========
这个是我被问得最多的问题：挖掘机和现在已经推出的各种分布式RPC、RMI框架有啥不同？
## HSF
好舒服框架由于和淘宝内部绑定过紧，依赖了ConfigServer?、Diamond、Notify等淘宝内部的系统，所以穿在别人身上一点也不舒服。目前没看到他的开源（还是开源了我不知道？NND）。不过我敢保证，就算开源了要在自己的场景中部署起来仍然是个痛苦的过程。 HSF框架由一群冲劲十足的骚男团队组成，爱玩飞镖。各种黑。不得不承认的是，HSF经过多次磨难，功能与性能早就不是他宣传的噱头。绝对的稳定、可靠、可控才是他赖以生存的标签。
## dubbo
对这个框架技术层面了解不多，所以无法做比较，不过口碑是相当不错。设计优异的各种扩展点让他拥有几乎无限的扩展能力。丰富的文档和QA让大家对这个框架信心爆棚。dubbo的实现几乎覆盖了当前分布式中你所有能想象到的问题，并且都很好的集成到了框架中，能有不同策略让你选择。dubbo可大可小，可以根据自己的需要进行裁减，任何其他开源的实现相比起dubbo来都黯然失色。也许只有过于复杂是我能“攻击”的点了。
## excavator（挖掘机）
挖掘机还处于一个起步阶段，代码精简，可定制化程度高。目前还不支持异步调用。专注于Java RMI，后续会路续支持QoS、路由规则／权重等策略。实现简单轻巧，不笨重。目前在内部项目中娱乐使用，求包养。
