/** This material is intended as a community resource and is licensed under the
  * Creative Commons Attribution 3.0 Unported License. Feel free to use, modify and share it.
  */
package scalatron.botwar

import MultiCommandParser.MultiCommand


/** A command issued by a bot, e.g. Move, Spawn, etc. */
sealed trait Command {
    def opcode: String
    def paramMap: Map[String,Any]
}
object Command
{
    case object Nop extends Command {
        def opcode = Protocol.PluginOpcode.Nop
        def paramMap = Map.empty
    }
    case class Move(offset: XY) extends Command {
        def opcode = Protocol.PluginOpcode.Move
        def paramMap = Map(Protocol.PluginOpcode.ParameterName.Direction -> offset)
    }
    case class Spawn(map: Map[String,String]) extends Command {
        def opcode = Protocol.PluginOpcode.Spawn
        def paramMap = map
    }
    case class Say(text: String) extends Command {
        def opcode = Protocol.PluginOpcode.Say
        def paramMap = Map(Protocol.PluginOpcode.ParameterName.Text -> text)
    }
    case class Status(text: String) extends Command {
        def opcode = Protocol.PluginOpcode.Status
        def paramMap = Map(Protocol.PluginOpcode.ParameterName.Text -> text)
    }
    case class Explode(blastRadius: Int) extends Command {
        def opcode = Protocol.PluginOpcode.Explode
        def paramMap = Map(Protocol.PluginOpcode.ParameterName.BlastRadius -> blastRadius)
    }
    case class Log(text: String) extends Command {
        def opcode = Protocol.PluginOpcode.Log
        def paramMap = Map(Protocol.PluginOpcode.ParameterName.Text -> text)
    }
    case class Set(map: Map[String,String]) extends Command {
        def opcode = Protocol.PluginOpcode.Set
        def paramMap = map
    }

    def fromControlFunctionResponse(controlFunctionResponse: String): Iterable[Command] = {
        val commandMap = MultiCommandParser.splitStringIntoParsedCommandMap(controlFunctionResponse)
        if(commandMap.isEmpty)
            Iterable(Command.Nop)       // player bots may return a Nop
        else
            fromMultiCommand(commandMap)
    }

    def fromMultiCommand(multiCommand: MultiCommand): Iterable[Command] =
        multiCommand.map(opcodeAndParams => fromOpcodeAndParams(opcodeAndParams._1, opcodeAndParams._2) )

    def fromOpcodeAndParams(opcode: String, params: Map[String,String]): Command = opcode match {
        case Protocol.PluginOpcode.Nop =>            // "Nop()"
            Nop

        case Protocol.PluginOpcode.Move =>            // "Move(dx=<int>,dy=<int>)"
            val direction = params.get(Protocol.PluginOpcode.ParameterName.Direction).map(s => XY(s)).getOrElse( XY.Zero )
            Move(direction)

        case Protocol.PluginOpcode.Spawn =>          // "Spawn(dx=<int>,dy=<int>,name=<int>,energy=<int>)"
            Spawn(params)

        case Protocol.PluginOpcode.Say =>            // "Say(text=<string>)"
            Say(params.get(Protocol.PluginOpcode.ParameterName.Text).getOrElse("?!?"))

        case Protocol.PluginOpcode.Status =>         // "Status(text=<string>)"
            Status(params.get(Protocol.PluginOpcode.ParameterName.Text).getOrElse("?!?"))

        case Protocol.PluginOpcode.Set =>            // "Set(key=string)"
            Set(params)

        case Protocol.PluginOpcode.Explode =>        // "Explode(size=<int>)"
            Explode(params.get(Protocol.PluginOpcode.ParameterName.BlastRadius).map(_.toInt.max(2)).getOrElse(5))

        case Protocol.PluginOpcode.Log =>            // "Log(text=<string>)"
            Log(params.get(Protocol.PluginOpcode.ParameterName.Text).getOrElse(""))

        case _ =>
            throw new IllegalStateException("unknown opcode: '" + opcode + "'")
    }
}