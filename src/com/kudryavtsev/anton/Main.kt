package com.kudryavtsev.anton

import com.kudryavtsev.anton.core.Accumulator
import com.kudryavtsev.anton.core.Memory
import com.kudryavtsev.anton.core.getCell
import java.io.BufferedReader
import java.io.FileReader
import java.io.PrintWriter
import java.io.Writer

private var accamulator = Accumulator()
private val memory = Memory()

fun main(args: Array<String>) {
    accamulator.clear()
    memory.clear()
    var stringBuilder = StringBuilder()

    val fileInputStream = BufferedReader(FileReader(args[0]))
    var buf: String? = ""
    val builder = StringBuilder()
    while (buf != null) {
        builder.append(buf).append("\n")
        buf = fileInputStream.readLine()
    }

    fileInputStream.close()

    val text = builder.toString()

    val commands = text.split("\n") as ArrayList<String>
    commands.removeIf {it == ""}
    commands.add("HLT")

    commands.filter { it.contains("DATA") }.forEach {
        val buf = it.split(" ") as ArrayList<String>
        val key = Integer.parseInt(buf[1], 16)
        val value = Integer.parseInt(buf[2], 16)
        memory.add(key, value)
    }

    commands.removeIf {it.contains("DATA")}

    var i = 0
    val timeStart = System.currentTimeMillis()
    while (commands[i] != "HLT") {
        val time = System.currentTimeMillis() - timeStart

        if (time >= 300000) {
            stringBuilder = StringBuilder()
            stringBuilder.append("Error... Endless loop")
            break
        }

        commands[i] = commands[i].substringBefore("//")

        while (commands[i].contains("  ")) {
            commands[i] = commands[i].replace("  ", " ")
        }

        val words = commands[i].split(" ") as ArrayList<String>

        when (words[0]) {
            "OUT" -> stringBuilder.append("${Integer.toHexString(accamulator.value)}\n")
            "LDA" -> accamulator.value = getCell(words[1], memory, accamulator)
            "STA" -> {
                var buf = words[1]
                var index: Int
                when {
                    words[1].contains("@") -> {
                        buf = buf.replace("@", "")
                        index = Integer.parseInt(buf, 16) + accamulator.x
                    }

                    words[1].contains("#") -> {
                        buf = buf.replace("#", "")
                        index = Integer.parseInt(buf, 16)
                    }

                    words[1].contains("*") -> {
                        buf = buf.replace("*", "")
                        index = Integer.parseInt(buf, 16)
                        index = memory[index]
                    }

                    else -> {
                        index = Integer.parseInt(buf, 16)
                    }
                }

                memory[index] = accamulator.value
            }
            "ADD"-> accamulator.value += getCell(words[1], memory, accamulator)
            "SUB"-> accamulator.value -= getCell(words[1], memory, accamulator)
            "BRA"-> {
                i = Integer.parseInt(words[1], 16)
                i--
            }

            "CPAX" -> accamulator.x = accamulator.value
            "CPAY" -> accamulator.y = accamulator.value

            "CPXA" -> accamulator.value = accamulator.x
            "CPYA" -> accamulator.value = accamulator.y

            "BRGT" -> {
                if (accamulator.value > 0) {
                    i = Integer.parseInt(words[1], 16)
                    i--
                }
            }

            "BRGE" -> {
                if (accamulator.value >= 0) {
                    i = Integer.parseInt(words[1], 16)
                    i--
                }
            }

            "BRLT" -> {
                if (accamulator.value < 0) {
                    i = Integer.parseInt(words[1], 16)
                    i--
                }
            }

            "BRLE" -> {
                if (accamulator.value <= 0) {
                    i = java.lang.Integer.parseInt(words[1], 16)
                    i--
                }
            }

            "BRNE" -> {
                if (accamulator.value != 0) {
                    i = Integer.parseInt(words[1], 16)
                    i--
                }
            }

            "LDX"-> accamulator.x = getCell(words[1], memory, accamulator)
            "LDY"-> accamulator.y = getCell(words[1], memory, accamulator)
        }
        i++
    }

    val res = stringBuilder.toString()
    val printer = PrintWriter(args[1])
    println(res)
    printer.print(res)
    printer.close()
}