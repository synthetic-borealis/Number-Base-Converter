package converter

import java.math.BigInteger
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.system.exitProcess

const val MAX_DIGITS = 5

fun main() {
    while (true) {
        print("Enter two numbers in format: {source base} {target base} (To quit type /exit) ")
        val baseInput = readln().lowercase()

        if (baseInput == "/exit") {
            exitProcess(0)
        } else {
            var sourceNumber = ""
            val (sourceBase, targetBase) = baseInput.split(" ").map { BigDecimal(it) }

            while (sourceNumber != "/back") {
                print("Enter number in base $sourceBase to convert to base $targetBase (To go back type /back) ")
                sourceNumber = readln().lowercase()

                if (sourceNumber != "/back") {
                    var result: String = convertBase(sourceNumber, sourceBase, targetBase, MAX_DIGITS)
                    if (!sourceNumber.contains('.') && result.contains('.')) {
                        result = result.split(".")[0]
                    }

                    println("Conversion result: $result")
                }
            }
        }
    }
}

fun convertBase(sourceNumber: String, sourceBase: BigDecimal, targetBase: BigDecimal, maxDigits: Int): String {
    return when (sourceBase) {
        targetBase -> {
            sourceNumber
        }
        BigDecimal.TEN -> {
            convertDecimalToBase(BigDecimal(sourceNumber), targetBase, maxDigits)
        }
        else -> {
            convertDecimalToBase(convertToDecimal(sourceNumber, sourceBase, maxDigits), targetBase, maxDigits)
        }
    }
}

fun convertToDecimal(sourceNumber: String, sourceBase: BigDecimal, maxDigits: Int): BigDecimal {
    val digitList = (('0'..'9') + ('A'..'Z')).toMutableList()
    var result = BigDecimal.ZERO

    // Convert fraction part
    if (sourceNumber.contains('.')) {
        val fractionSegment = sourceNumber.split(".")[1].reversed().uppercase()
        val digitChars = fractionSegment.split("").toMutableList().filter { it.isNotEmpty() }.map { it[0] }
        val digitValues = digitChars.map { BigDecimal(digitList.indexOf(it)) }.reversed()

        for (i in 1 until digitValues.size) {
            result += digitValues[i - 1] * sourceBase.pow(-i, MathContext.DECIMAL64)
        }
    }

    // Convert integer part
    val integerSegment = sourceNumber.split(".")[0].uppercase()
    val numberInteger = convertToDecimalInteger(integerSegment, sourceBase.toBigInteger())

    result = result.add(numberInteger.toBigDecimal())

    return result.setScale(maxDigits, RoundingMode.HALF_DOWN)
}

fun convertDecimalToBase(sourceNumber: BigDecimal, targetBase: BigDecimal, maxDigits: Int): String {
    val digitList = (('0'..'9') + ('A'..'Z')).toMutableList()

    // Convert fraction part
    var numberFraction = sourceNumber.remainder(BigDecimal.ONE)
    val numberInteger = (sourceNumber - numberFraction).toBigInteger()
    var numOfDigits = 0
    var fractionResult = ""

    // Convert integer part
    var integerResult = convertDecimalToTargetInteger(numberInteger, targetBase.toBigInteger())
    if (integerResult.isEmpty()) {
        integerResult = "0"
    }

    if (numberFraction != BigDecimal.ZERO) {
        while (numberFraction != BigDecimal.ZERO && numOfDigits < maxDigits) {
            val digitIndex = (numberFraction * targetBase).toInt()
            fractionResult += digitList[digitIndex]
            numberFraction = numberFraction.multiply(targetBase).remainder(BigDecimal.ONE)
            numOfDigits++
        }

        fractionResult = fractionResult.dropLastWhile { it == '0' }
    }

    while (fractionResult.length < maxDigits) {
        fractionResult += '0'
    }

    return "$integerResult.$fractionResult"
}

fun convertToDecimalInteger(sourceNumber: String, sourceBase: BigInteger): BigInteger {
    val digitList = (('0'..'9') + ('A'..'Z')).toMutableList()
    val digits = sourceNumber.uppercase().split("").filter { it != "" }.map { it[0] }.reversed()
    var result = BigInteger.ZERO

    for (i in digits.indices) {
        result += digitList.indexOf(digits[i]).toBigInteger() * sourceBase.pow(i)
    }

    return result
}

fun convertDecimalToTargetInteger(sourceNumber: BigInteger, targetBase: BigInteger): String {
    val digitList = (('0'..'9') + ('A'..'Z')).toMutableList()
    var num = sourceNumber
    var result = ""

    while (num > BigInteger.ZERO) {
        val digitIndex = (num % targetBase).toInt()
        result = digitList[digitIndex].toString() + result
        num /= targetBase
    }

    return result
}