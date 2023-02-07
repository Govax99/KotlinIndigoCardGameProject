import org.hyperskill.hstest.dynamic.DynamicTest
import org.hyperskill.hstest.stage.StageTest
import org.hyperskill.hstest.testcase.CheckResult
import org.hyperskill.hstest.testing.TestedProgram

class ErrorData(val correct: Boolean, val errorMsg: String,
                val topCard: String = "", val cardsList: List<String> = emptyList())

class CardGameTest : StageTest<Any>() {

    @DynamicTest
    fun playFirstNormalExeTest5(): CheckResult {
        repeat(5) {
            val deck = mutableListOf<String>()
            val cardsInHand = mutableListOf<String>()
            var pointsPlayer = 0
            var pointsComputer = 0
            var pointsOnTable = 0
            var numOfCardsPlayer = 0
            var numOfCardsComputer = 0
            var numOfCardsOnTable = 0
            var whoWon = 0

            val main = TestedProgram()
            var outputString = main.start().trim()
            var position = checkOutput(outputString.lowercase(), 0, "Indigo Card Game".lowercase())
            if (position == -1) return CheckResult(false, "Wrong game title.")
            position = checkOutput(outputString.lowercase(), position, "Play first?".lowercase())
            if (position == -1) return CheckResult(false, "Wrong play first prompt.")

            outputString = main.execute("yes").trim()
            var validOutput = checkInitial(outputString)
            if (!validOutput.correct) return CheckResult(false, validOutput.errorMsg)
            deck.addAll(validOutput.cardsList)
            var topCard = validOutput.topCard

            outputString = outputString.substringAfter(topCard).trim()
            numOfCardsOnTable = 4
            pointsOnTable = countPoints(validOutput.cardsList)
            repeat(4) {
                for (numOfCards in 6 downTo 1) {
                    validOutput = checkPlayerOutput2(outputString, numOfCards, numOfCardsOnTable, topCard)
                    if (!validOutput.correct) return CheckResult(false, validOutput.errorMsg)
                    if (numOfCards == 6) {
                        cardsInHand.clear()
                        cardsInHand.addAll(validOutput.cardsList)
                        for (card in cardsInHand)
                            if (deck.contains(card))
                                return CheckResult(
                                    false,
                                    "Some cards in hand have already passed on table (Duplicates)."
                                )
                        deck.addAll(cardsInHand)
                    } else {
                        if (!cardsInHand.containsAll(validOutput.cardsList))
                            return CheckResult(false, "Cards in hand have changed since the last card was played.")
                    }
                    val cardToPlay = chooseCards(validOutput.cardsList, topCard).first()
                    var hasWon = if (numOfCardsOnTable == 0) false
                    else {
                        val a = getRankSuit(topCard)
                        val b = getRankSuit(cardToPlay)
                        a.first == b.first || a.second == b.second
                    }

                    topCard = cardToPlay
                    numOfCardsOnTable++
                    pointsOnTable += countPoints(listOf(topCard))
                    cardsInHand.remove(topCard)
                    outputString = main.execute("${validOutput.cardsList.indexOf(topCard) + 1}").trim()
                    if (hasWon) {
                        whoWon = 0
                        pointsPlayer += pointsOnTable
                        pointsOnTable = 0
                        numOfCardsPlayer += numOfCardsOnTable
                        numOfCardsOnTable = 0
                        topCard = ""
                        position = checkOutput(outputString.lowercase(), 0, "Player wins cards".lowercase())
                        if (position == -1) return CheckResult(false, "Wrong player wins cards message.")
                        position = checkOutput(
                            outputString.lowercase(), position,
                            "Score: Player $pointsPlayer - Computer $pointsComputer".lowercase(),
                            "Cards: Player $numOfCardsPlayer - Computer $numOfCardsComputer".lowercase()
                        )
                        if (position == -1) return CheckResult(false, "Wrong score output.")
                        outputString =
                            outputString.substringAfter("Cards: Player $numOfCardsPlayer - Computer $numOfCardsComputer")
                                .trim()
                    }

                    validOutput = checkComputerOutput3(outputString, numOfCardsOnTable, topCard, numOfCards)
                    if (!validOutput.correct) return CheckResult(false, validOutput.errorMsg)
                    hasWon = if (numOfCardsOnTable == 0) false
                    else {
                        val a = getRankSuit(topCard)
                        val b = getRankSuit(validOutput.topCard)
                        (a.first == b.first) || (a.second == b.second)
                    }
                    topCard = validOutput.topCard
                    if (deck.contains(topCard))
                        return CheckResult(false, "Computer played card is a duplicate.")
                    deck.add(topCard)
                    numOfCardsOnTable++
                    pointsOnTable += countPoints(listOf(topCard))
                    outputString = outputString.substringAfter(topCard).trim()
                    outputString = outputString.substringAfter(topCard).trim()
                    if (hasWon) {
                        whoWon = 1
                        pointsComputer += pointsOnTable
                        pointsOnTable = 0
                        numOfCardsComputer += numOfCardsOnTable
                        numOfCardsOnTable = 0
                        position = checkOutput(outputString.lowercase(), 0, "Computer wins cards".lowercase())
                        if (position == -1) return CheckResult(false, "Wrong computer wins cards message.")
                        position = checkOutput(
                            outputString.lowercase(), position,
                            "Score: Player $pointsPlayer - Computer $pointsComputer".lowercase(),
                            "Cards: Player $numOfCardsPlayer - Computer $numOfCardsComputer".lowercase()
                        )
                        if (position == -1) return CheckResult(false, "Wrong score output.")
                        outputString =
                            outputString.substringAfter("Cards: Player $numOfCardsPlayer - Computer $numOfCardsComputer")
                                .trim()
                    }
                }
            }

            position =
                if (numOfCardsOnTable == 0) checkOutput(outputString.lowercase(), 0, "No cards on the table".lowercase())
                else checkOutput(
                    outputString.lowercase(),
                    0,
                    "$numOfCardsOnTable cards on the table, and the top card is $topCard".lowercase()
                )
            if (position == -1) return CheckResult(false, "Wrong output for number of cards or the top card.")
            if (whoWon == 0) {
                pointsPlayer += pointsOnTable
                numOfCardsPlayer += numOfCardsOnTable
            } else {
                pointsComputer += pointsOnTable
                numOfCardsComputer += numOfCardsOnTable
            }
            if (numOfCardsPlayer >= numOfCardsComputer) pointsPlayer += 3
            else pointsComputer += 3
            position = checkOutput(
                outputString.lowercase(), position,
                "Score: Player $pointsPlayer - Computer $pointsComputer".lowercase(),
                "Cards: Player $numOfCardsPlayer - Computer $numOfCardsComputer".lowercase(),
                "Game Over".lowercase()
            )
            if (position == -1) return CheckResult(false, "Wrong score output.")
            if (!main.isFinished) return CheckResult(false, "Application hasn't exited after exit command.")
        }
        return CheckResult.correct()
    }

    @DynamicTest
    fun playSecondNormalExeTest5(): CheckResult {
        repeat(5) {
            val deck = mutableListOf<String>()
            val cardsInHand = mutableListOf<String>()
            var pointsPlayer = 0
            var pointsComputer = 0
            var pointsOnTable = 0
            var numOfCardsPlayer = 0
            var numOfCardsComputer = 0
            var numOfCardsOnTable = 0
            var whoWon = 0

            val main = TestedProgram()
            var outputString = main.start().trim()
            var position = checkOutput(outputString.lowercase(), 0, "Indigo Card Game".lowercase())
            if (position == -1) return CheckResult(false, "Wrong game title.")
            position = checkOutput(outputString.lowercase(), position, "Play first?".lowercase())
            if (position == -1) return CheckResult(false, "Wrong play first prompt.")

            outputString = main.execute("no").trim()
            var validOutput = checkInitial(outputString)
            if (!validOutput.correct) return CheckResult(false, validOutput.errorMsg)
            deck.addAll(validOutput.cardsList)
            var topCard = validOutput.topCard

            outputString = outputString.substringAfter(topCard).trim()
            numOfCardsOnTable = 4
            pointsOnTable = countPoints(validOutput.cardsList)
            repeat(4) {
                for (numOfCards in 6 downTo 1) {
                    validOutput = checkComputerOutput3(outputString, numOfCardsOnTable, topCard, numOfCards)
                    if (!validOutput.correct) return CheckResult(false, validOutput.errorMsg)
                    var hasWon = if (numOfCardsOnTable == 0) false
                    else {
                        val a = getRankSuit(topCard)
                        val b = getRankSuit(validOutput.topCard)
                        (a.first == b.first) || (a.second == b.second)
                    }
                    topCard = validOutput.topCard
                    if (deck.contains(topCard))
                        return CheckResult(false, "Computer played card is a duplicate.")
                    deck.add(topCard)
                    numOfCardsOnTable++
                    pointsOnTable += countPoints(listOf(topCard))
                    outputString = outputString.substringAfter(topCard).trim()
                    outputString = outputString.substringAfter(topCard).trim()
                    if (hasWon) {
                        whoWon = 1
                        pointsComputer += pointsOnTable
                        pointsOnTable = 0
                        numOfCardsComputer += numOfCardsOnTable
                        numOfCardsOnTable = 0
                        position = checkOutput(outputString.lowercase(), 0, "Computer wins cards".lowercase())
                        if (position == -1) return CheckResult(false, "Wrong computer wins cards message.")
                        position = checkOutput(
                            outputString.lowercase(), position,
                            "Score: Player $pointsPlayer - Computer $pointsComputer".lowercase(),
                            "Cards: Player $numOfCardsPlayer - Computer $numOfCardsComputer".lowercase()
                        )
                        if (position == -1) return CheckResult(false, "Wrong score output.")
                        outputString =
                            outputString.substringAfter("Cards: Player $numOfCardsPlayer - Computer $numOfCardsComputer")
                                .trim()
                    }

                    validOutput = checkPlayerOutput2(outputString, numOfCards, numOfCardsOnTable, topCard)
                    if (!validOutput.correct) return CheckResult(false, validOutput.errorMsg)
                    if (numOfCards == 6) {
                        cardsInHand.clear()
                        cardsInHand.addAll(validOutput.cardsList)
                        for (card in cardsInHand)
                            if (deck.contains(card))
                                return CheckResult(
                                    false,
                                    "Some cards in hand have already passed on table (Duplicates)."
                                )
                        deck.addAll(cardsInHand)
                    } else {
                        if (!cardsInHand.containsAll(validOutput.cardsList))
                            return CheckResult(false, "Cards in hand have changed since the last card was played.")
                    }
                    val cardToPlay = chooseCards(validOutput.cardsList, validOutput.topCard).first()
                    hasWon = if (numOfCardsOnTable == 0) false
                    else {
                        val a = getRankSuit(topCard)
                        val b = getRankSuit(cardToPlay)
                        a.first == b.first || a.second == b.second
                    }

                    topCard = cardToPlay
                    numOfCardsOnTable++
                    pointsOnTable += countPoints(listOf(topCard))
                    cardsInHand.remove(topCard)
                    outputString = main.execute("${validOutput.cardsList.indexOf(topCard) + 1}").trim()
                    if (hasWon) {
                        whoWon = 0
                        pointsPlayer += pointsOnTable
                        pointsOnTable = 0
                        numOfCardsPlayer += numOfCardsOnTable
                        numOfCardsOnTable = 0
                        topCard = ""
                        position = checkOutput(outputString.lowercase(), 0, "Player wins cards".lowercase())
                        if (position == -1) return CheckResult(false, "Wrong player wins cards message.")
                        position = checkOutput(
                            outputString.lowercase(), position,
                            "Score: Player $pointsPlayer - Computer $pointsComputer".lowercase(),
                            "Cards: Player $numOfCardsPlayer - Computer $numOfCardsComputer".lowercase()
                        )
                        if (position == -1) return CheckResult(false, "Wrong score output.")
                        outputString =
                            outputString.substringAfter("Cards: Player $numOfCardsPlayer - Computer $numOfCardsComputer")
                                .trim()
                    }
                }
            }

            position =
                if (numOfCardsOnTable == 0) checkOutput(outputString.lowercase(), 0, "No cards on the table".lowercase())
                else checkOutput(
                    outputString.lowercase(),
                    0,
                    "$numOfCardsOnTable cards on the table, and the top card is $topCard".lowercase()
                )
            if (position == -1) return CheckResult(false, "Wrong output for number of cards or the top card.")
            if (whoWon == 0) {
                pointsPlayer += pointsOnTable
                numOfCardsPlayer += numOfCardsOnTable
            } else {
                pointsComputer += pointsOnTable
                numOfCardsComputer += numOfCardsOnTable
            }
            if (numOfCardsPlayer >= numOfCardsComputer) pointsPlayer += 3
            else pointsComputer += 3
            position = checkOutput(
                outputString.lowercase(), position,
                "Score: Player $pointsPlayer - Computer $pointsComputer".lowercase(),
                "Cards: Player $numOfCardsPlayer - Computer $numOfCardsComputer".lowercase(),
                "Game Over".lowercase()
            )
            if (position == -1) return CheckResult(false, "Wrong score output.")
            if (!main.isFinished) return CheckResult(false, "Application hasn't exited after exit command.")
        }
        return CheckResult.correct()
    }

    @DynamicTest
    fun playFirstWrongInputTest(): CheckResult {
        val main = TestedProgram()
        var outputString = main.start().trim()
        var position = checkOutput(outputString.lowercase(), 0, "Indigo Card Game".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong game title.")
        position = checkOutput(outputString.lowercase(), position, "Play first?".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong play first prompt.")

        outputString = main.execute("Hello").trim()
        position = checkOutput(outputString.lowercase(), 0, "Play first?".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong output after wrong input after the play first question.")

        outputString = main.execute("me").trim()
        position = checkOutput(outputString.lowercase(), 0, "Play first?".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong output after wrong input after the play first question.")

        outputString = main.execute("0").trim()
        position = checkOutput(outputString.lowercase(), 0, "Play first?".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong output after wrong input after the play first question.")

        outputString = main.execute("play").trim()
        position = checkOutput(outputString.lowercase(), 0, "Play first?".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong output after wrong input after the play first question.")

        outputString = main.execute("yes").trim()
        var validOutput = checkInitial(outputString)
        if ( !validOutput.correct) return CheckResult(false, validOutput.errorMsg)
        val topCard = validOutput.topCard

        outputString = outputString.substringAfter(topCard).trim()
        validOutput = checkPlayerOutput2(outputString, 6, 4, topCard)
        if ( !validOutput.correct) return CheckResult(false, validOutput.errorMsg)

        outputString = main.execute("exit").trim()
        position = checkOutput(outputString.lowercase(), 0, "Game Over".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong exit message.")

        if (!main.isFinished) return CheckResult(false, "Application hasn't exited after exit command.")

        return CheckResult.correct()
    }

    @DynamicTest
    fun wrongCardToPlayTest(): CheckResult {
        val main = TestedProgram()
        var outputString = main.start().trim()
        var position = checkOutput(outputString.lowercase(), 0, "Indigo Card Game".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong game title.")
        position = checkOutput(outputString.lowercase(), position, "Play first?".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong play first prompt.")

        outputString = main.execute("yes").trim()
        var validOutput = checkInitial(outputString)
        if ( !validOutput.correct) return CheckResult(false, validOutput.errorMsg)
        val topCard = validOutput.topCard

        outputString = outputString.substringAfter(topCard).trim()
        validOutput = checkPlayerOutput2(outputString, 6, 4, topCard)
        if ( !validOutput.correct) return CheckResult(false, validOutput.errorMsg)

        outputString = main.execute("0").trim()
        position = checkOutput(outputString.lowercase(), 0, "Choose a card to play (1-6):".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong output, after input an invalid card number.")

        outputString = main.execute("7").trim()
        position = checkOutput(outputString.lowercase(), 0, "Choose a card to play (1-6):".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong output, after input an invalid card number.")

        outputString = main.execute("One").trim()
        position = checkOutput(outputString.lowercase(), 0, "Choose a card to play (1-6):".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong output, after input an invalid card number.")


        outputString = main.execute("exit").trim()
        position = checkOutput(outputString.lowercase(), 0, "Game Over".lowercase())
        if ( position  == -1 ) return CheckResult(false, "Wrong exit message.")

        if (!main.isFinished) return CheckResult(false, "Application hasn't exited after exit command.")

        return CheckResult.correct()
    }

}

fun checkOutput(outputString: String, searchPos: Int, vararg checkStr: String): Int {
    var searchPosition = searchPos
    for (str in checkStr) {
        val findPosition = outputString.indexOf(str, searchPosition)
        if (findPosition == -1) return -1
        if ( outputString.substring(searchPosition until findPosition).isNotBlank() ) return -1
        searchPosition = findPosition + str.length
    }
    return searchPosition
}

fun checkOIfValidCards2(cardsStr: String, numOfCards: Int): Boolean {
    val cards = cardsStr.split(" ")
    val cardRegex = "(A|[2-9]|10|J|Q|K)(♦|♥|♠|♣)".toRegex()
    for (card in cards) if (!card.matches(cardRegex)) {
        return false
    }
    return cards.size == numOfCards
}

fun checkIfUniqueCards(outputString: String): Boolean {
    val lines = outputString.lines()
    val cards = lines.first().trim().split(" ")
    return cards.distinct().size == cards.size
}

fun checkOIfValidCardsInHand(cardsStr: String, numOfCards: Int): Boolean {
    val cards = cardsStr.split(" ")
    val cardRegex = "([1-6])\\)(A|[2-9]|10|J|Q|K)(♦|♥|♠|♣)".toRegex()
    for (card in cards) if (!card.matches(cardRegex)) {
        return false
    }
    return cards.size == numOfCards
}

fun checkInitial(output: String): ErrorData {
    val position = checkOutput(output.lowercase(), 0, "Initial cards on the table:".lowercase())
    if ( position  == -1 ) return ErrorData(false, "Wrong Initial cards message.")
    val cardsStr = output.substring(position).lines().map { it.trim() }.first { it != "" }
    if (cardsStr.isEmpty()) return ErrorData(false, "No initial cards are printed.")
    if ( !checkOIfValidCards2(cardsStr, 4) ) return ErrorData(false, "Invalid initial cards.")
    if ( !checkIfUniqueCards(cardsStr) ) return ErrorData(false, "Initial cards contain duplicate cards.")
    val cardsOnTable = cardsStr.trim().split(" ")
    val topCard = cardsOnTable.last()
    return ErrorData(true, "", topCard, cardsOnTable)
}

fun checkPlayerOutput2(output: String, numOfCards: Int, numOfCardsOnTable : Int, topCard: String): ErrorData {
    var position = if (numOfCardsOnTable == 0) checkOutput(output.lowercase(), 0, "No cards on the table".lowercase())
    else checkOutput(output.lowercase(), 0, "$numOfCardsOnTable cards on the table, and the top card is $topCard".lowercase())
    if ( position  == -1 ) return ErrorData(false, "Player turn: Wrong message for number of cards or the top card.")

    position = checkOutput(output.lowercase(), position, "Cards in hand:".lowercase())
    if ( position  == -1 ) return ErrorData(false, "Wrong cards in hand message.")
    val cardsInHand = output.substring(position).lines().map { it.trim() }.firstOrNull() { it != "" }
        ?: return ErrorData(false, "Wrong cards in hand message.")
    if (!checkOIfValidCardsInHand(cardsInHand, numOfCards)) return ErrorData(false, "Wrong cards in hand message.")
    val listCardsInHand = cardsInHand.split(" ").map { it.substring(2) }
    var strCardsInHand = ""
    for (card in listCardsInHand) strCardsInHand += "$card "
    if ( !checkIfUniqueCards(strCardsInHand) ) return ErrorData(false, "Player's cards in hand contain duplicate cards.")
    val lastCard = listCardsInHand.last()
    position = output.indexOf(lastCard, position)
    position = checkOutput(output.lowercase(), position + lastCard.length, "Choose a card to play (1-$numOfCards):".lowercase())
    if ( position  == -1 ) return ErrorData(false, "Wrong prompt to choose a card.")
    if (numOfCards == 6 && checkIfSequentialCards(listCardsInHand)) return ErrorData(false, "The card deck isn't shuffled.")
    return ErrorData(true, "", cardsList = listCardsInHand)
}

fun checkComputerOutput3(output: String, numOfCardsOnTable : Int, topCard: String, numOfCards: Int): ErrorData {
    var position = if (numOfCardsOnTable == 0) checkOutput(output.lowercase(), 0, "No cards on the table".lowercase())
    else checkOutput(output.lowercase(), 0, "$numOfCardsOnTable cards on the table, and the top card is $topCard".lowercase())
    if ( position  == -1 ) return ErrorData(false, "Computer turn: Wrong output for number of cards or the top card.")

    val cardsStr = output.substring(position).lines().map { it.trim() }.firstOrNull() { it != "" }
        ?: return ErrorData(false, "The computer's cards in hand are not printed.")
    if (cardsStr.isEmpty()) return ErrorData(false, "The computer's cards in hand are not printed.")
    if ( !checkOIfValidCards2(cardsStr, numOfCards) ) return ErrorData(false, "Invalid computer's cards in hand.")
    if ( !checkIfUniqueCards(cardsStr) ) return ErrorData(false, "Computer's cards in hand contain duplicates.")

    val cardsInHand = cardsStr.trim().split(" ")
    position = output.indexOf('\n', output.indexOf(cardsStr))

    position = checkOutput(output.lowercase(), position, "Computer plays".lowercase())
    if ( position  == -1 ) return ErrorData(false, "Wrong computer plays a card message.")

    val endIndex = output.indexOf("\n", position)
    if (endIndex < 0) return ErrorData(false, "Wrong output. Some lines are missing")
    val card = output.substring(position, endIndex).trim()

    val candidates = chooseCards(cardsInHand, topCard)
//    println("****************")
//    println("${cardsInHand}")
//    println("$topCard")
//    println("$candidates}")
//    println("****************")
    if (!candidates.contains(card)) return ErrorData(false, "Computer played card $card instead one of $candidates.")

    if (!checkOIfValidCards2(card, 1)) return ErrorData(false, "Computer played an invalid card.")
    if (numOfCards == 6 && checkIfSequentialCards(cardsInHand)) return ErrorData(false, "The card deck isn't shuffled.")
    return ErrorData(true, "", card, cardsInHand)
}

fun checkIfSequentialCards(cards: List<String>): Boolean {
    if (cards.size < 2) return false

    val suit = getSuit(cards.first())
    var hasSameSuit = true
    for (index in 1 until cards.size) if (suit != getSuit(cards[index])) hasSameSuit = false
    if (!hasSameSuit) return false

    val ranks = listOf("a", "2", "3", "4", "5", "6", "7", "8", "9", "10", "j", "q", "k")
    var initialRank = ranks.indexOf( getRank( cards.first() ).lowercase() )
    var sequentialUp = true
    for (index in 1 until cards.size) {
        val nextIndex = initialRank++
        if (initialRank > 12) initialRank = 0
        if (initialRank != ranks.indexOf( getRank( cards[index] ).lowercase() ))
            sequentialUp = false
    }

    initialRank = ranks.indexOf( getRank( cards.first() ).lowercase() )
    var sequentialDown = true
    for (index in 1 until cards.size) {
        val nextIndex = initialRank--
        if (initialRank < 0) initialRank = 12
        if (initialRank != ranks.indexOf( getRank( cards[index] ).lowercase() ))
            sequentialDown = false
    }

    return sequentialUp || sequentialDown
}

fun getRankSuit(card:String): Pair<String, String> {
    return if (card.length == 2) Pair(card.substring(0, 1), card.substring(1, 2))
    else Pair(card.substring(0, 2), card.substring(2, 3))
}

fun countPoints(cards: List<String>): Int {
    val ranks = listOf("A", "10", "J", "Q", "K")
    var count = 0
    for (card in cards) {
        val rank = if (card.length == 2) card.substring(0, 1)
        else card.substring(0, 2)
        if (rank in ranks) count++
    }
    return count
}

fun chooseCards(cardsInHand: List<String>, topCard: String): List<String> {
    // Only one card at hand
    if (cardsInHand.size == 1) return cardsInHand

    // No cards on the table. Choose first same suit, then same rank, then random
    if (topCard == "") {
        var cardsToPlay = getSameSuit(cardsInHand)
        if (cardsToPlay.isNotEmpty()) return cardsToPlay
        cardsToPlay = getSameRank(cardsInHand)
        if (cardsToPlay.isNotEmpty()) return cardsToPlay
        else return cardsInHand
    }

    // Get candidate cards
    var candidates = mutableListOf<String>()
    val topRank = getRank(topCard)
    val topSuit = getSuit(topCard)
    for (card in cardsInHand) {
        if (getRank(card) == topRank || getSuit(card) == topSuit) candidates.add(card)
    }

    if (candidates.size == 0) {                       // No candidate cards
        var cardsToPlay = getSameSuit(cardsInHand)
        if (cardsToPlay.isNotEmpty()) return cardsToPlay
        cardsToPlay = getSameRank(cardsInHand).toMutableList()
        if (cardsToPlay.isNotEmpty()) return cardsToPlay
        else return cardsInHand
    }
    else if (candidates.size == 1) {                // Just one candidate card
        return candidates
    } else {                                          // Two or more candidate cards
        var cardsToPlay = getSameSuit(candidates)
        if (cardsToPlay.isNotEmpty()) return cardsToPlay
        cardsToPlay = getSameRank(candidates)
        if (cardsToPlay.isNotEmpty()) return cardsToPlay
        else return candidates
    }
//    return cardsInHandComputer
}

fun getSameSuit(cards: List<String>): List<String> {
    val similarCards = mutableListOf<String>()
    for (suit in listOf("♠", "♥", "♦", "♣")) {
        val c = cards.filter{ getSuit(it) == suit }
        if (c.size > 1) similarCards.addAll(c)
    }
    return similarCards
}

fun getSameRank(cards: List<String>): List<String> {
    val similarCards = mutableListOf<String>()
    for (rank in listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")) {
        val r = cards.filter { getRank(it) == rank }
        if (r.size > 1) similarCards.addAll(r)
    }
    return similarCards
}

fun getRank(card: String): String {
    return if (card.length == 2) card.substring(0, 1)
    else card.substring(0, 2)
}

fun getSuit(card: String): String {
    return if (card.length == 2) card.substring(1, 2)
    else card.substring(2, 3)
}


