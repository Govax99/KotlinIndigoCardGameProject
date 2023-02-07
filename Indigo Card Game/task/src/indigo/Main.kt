package indigo

import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.lang.IndexOutOfBoundsException
import java.lang.NumberFormatException
import kotlin.system.exitProcess


class Deck(var cards: MutableList<String> = mutableListOf()) {

    fun resetDeck() {
        val tmpRanks = mutableListOf<String>("A")
        tmpRanks.addAll((2..10).toList().map { it.toString() })
        tmpRanks.addAll(listOf("J", "Q", "K"))
        val ranks = tmpRanks.toList()

        val suits = listOf<String>("♦", "♥", "♠", "♣")
        cards = suits.flatMap { a -> ranks.map { it + a } }.toMutableList()
    }

    fun shuffleDeck() {
        cards.shuffle()
    }

    fun getCardsFromDeck() {
        println("Number of cards:")
        try {
            val num = readln().toInt()
            if (num !in 1..52) {
                throw IllegalStateException()
            }
            try {
                val extractedCards = cards.take(num)
                cards = cards.drop(num).toMutableList()

                println(extractedCards.joinToString(" "))
            } catch (e: IllegalArgumentException) {
                println("The remaining cards are insufficient to meet the request.")
            }
        } catch (e: IllegalStateException) {
            println("Invalid number of cards.")
        } catch (e: NumberFormatException) {
            println("Invalid number of cards.")
        }
    }

    fun getCardsSilent(numCards: Int): MutableList<String> {
        try {
            if (numCards !in 1..52) {
                throw IllegalStateException()
            }
            try {
                val extractedCards = cards.take(numCards).toMutableList()
                cards = cards.drop(numCards).toMutableList()

                return extractedCards
            } catch (e: IllegalArgumentException) {
                println("The remaining cards are insufficient to meet the request.")
                return mutableListOf()
            }
        } catch (e: IllegalStateException) {
            println("Invalid number of cards.")
            return mutableListOf()
        } catch (e: NumberFormatException) {
            println("Invalid number of cards.")
            return mutableListOf()
        }
    }

    fun isDeckEmpty(): Boolean {
        return cards.isEmpty()
    }

}

open class Stack(var elements: MutableList<String> = mutableListOf()) {

    fun peek(): String {
        return elements.last()
    }

    fun pop(): String {
        val poppedElement = elements.last()
        elements.removeLast()
        return poppedElement
    }

    fun push(element: String) {
        elements.add(element)
    }

    fun pushMany(elementsList: MutableList<String>) {
        elements.addAll(elementsList)
    }

}

class Table(elements: MutableList<String>): Stack(elements) {
    fun tableClear(): MutableList<String> {
        val cards = this.elements
        this.elements = mutableListOf()
        return  cards
    }

    fun isTableEmpty(): Boolean {
        return elements.isEmpty()
    }
}


fun isMatch(card1: String, card2: String): Boolean {
    return (card1.last() == card2.last()) || (card1.dropLast(1) == card2.dropLast(1))
}

open class User(var inHand: MutableList<String>, var wonCards: MutableList<String>) {
    var wonLast = false

    fun dealCards(deck: Deck): Deck {
        val extractedCards = deck.getCardsSilent(6)
        inHand.addAll(extractedCards)
        return deck
    }

    fun isHandEmpty(): Boolean {
        return inHand.isEmpty()
    }
}

fun isPointCard(card: String): Boolean {
    val rankPoints = mutableListOf<String>("A", "10", "J", "Q", "K")
    return card.dropLast(1) in rankPoints
}

fun isCandidateCard(card: String, table: Table): Boolean {
    if (table.isTableEmpty()) return true
    return isMatch(card, table.peek())
}

fun isSameSuit(card: String, table: Table): Boolean {
    return (card.last() == table.peek().last())
}

fun isSameRank(card: String, table: Table): Boolean {
    return (card.dropLast(1) == table.peek().dropLast(1))
}

fun updateScores(user: User, computer: Computer) {
    val numCardsUser = user.wonCards.size
    val numCardsComputer = computer.wonCards.size
    val scoreUser = user.wonCards.filter { isPointCard(it) }.size
    val scoreComputer = computer.wonCards.filter { isPointCard(it) }.size
    println("Score: Player $scoreUser - Computer $scoreComputer")
    println("Cards: Player $numCardsUser - Computer $numCardsComputer")
}

fun updateScoresLast(user: User, computer: Computer) {
    val numCardsUser = user.wonCards.size
    val numCardsComputer = computer.wonCards.size
    var scoreUser = user.wonCards.filter { isPointCard(it) }.size
    var scoreComputer = computer.wonCards.filter { isPointCard(it) }.size
    if (numCardsUser + numCardsComputer == 52) {
        if (numCardsUser >= numCardsComputer) {
            scoreUser+=3
        } else {
            scoreComputer+=3
        }
    }
    println("Score: Player $scoreUser - Computer $scoreComputer")
    println("Cards: Player $numCardsUser - Computer $numCardsComputer")
}

class Player(inHand: MutableList<String> = mutableListOf(), wonCards: MutableList<String> = mutableListOf()): User(inHand, wonCards) {

    fun takeTurn(table: Table, computer: Computer): Table {
        if (table.isTableEmpty()) {
            println("No cards on the table")
        } else {
            println("${table.elements.size} cards on the table, and the top card is ${table.peek()}")
        }
        val size = inHand.size
        print("Cards in hand: ")
        for ((index, value) in inHand.withIndex()) {
            print("${index + 1})${value} ")
        }
        println()
        println("Choose a card to play (1-${size}):")
        while(true) {
            val input = readln()
            if (input == "exit") {
                println("Game Over")
                exitProcess(1)
            }
            val choice: Int
            try {
                choice = input.toInt()
            } catch (e: NumberFormatException) {
                println("Choose a card to play (1-${size}):")
                continue
            }
            if (choice in 1..size) {
                if (!table.isTableEmpty() && isMatch(table.peek(), inHand[choice - 1])) {
                    table.push(inHand[choice - 1])
                    inHand.removeAt(choice - 1)
                    val cardsFromTable = table.tableClear()
                    wonCards.addAll(cardsFromTable)
                    println("Player wins cards")
                    this.wonLast = true
                    updateScores(this, computer)
                    break
                } else {
                    table.push(inHand[choice - 1])
                    inHand.removeAt(choice - 1)
                    break
                }
            } else {
                println("Choose a card to play (1-${size}):")
            }
        }
        return table
    }


}

class Computer(inHand: MutableList<String> = mutableListOf(), wonCards: MutableList<String> = mutableListOf()): User(inHand, wonCards) {

    fun chooseStrategy(table: Table): Int {
        if (inHand.size == 1) {
            //println("Strategy 1")
            return 0
        }
        val inHandCandidate = inHand.filter { isCandidateCard(it, table) }
        val inHandSameSuit = inHand.groupBy { it.last() }.values.filter { it.size > 1 }.flatMap { it.distinct() }
        val inHandSameRank = inHand.groupBy { it.dropLast(1) }.values.filter { it.size > 1 }.flatMap { it.distinct() }

        /*print("In Hand Candidates: ")
        println(inHandCandidate)
        print("In Hand Same Suit: ")
        println(inHandSameSuit)
        print("In Hand Same Rank: ")
        println(inHandSameRank)*/


        if (inHandCandidate.size == 1) {
            //println("Strategy 2")
            return inHand.indexOf(inHandCandidate.first())
        }
        if (table.isTableEmpty() || (!table.isTableEmpty() && inHandCandidate.isEmpty())) {
            if (inHandSameSuit.isNotEmpty()) {
                //println("Strategy 3a")
                val card = inHandSameSuit.random()
                return inHand.indexOf(card)
            }
            if (inHandSameRank.isNotEmpty()) {
                //println("Strategy 3b")
                val card = inHandSameRank.random()
                return  inHand.indexOf(card)
            }
            //println("Strategy 3c")
            return inHand.indexOf(inHand.random())
        }
        if (inHandCandidate.size >= 2) {
            if (inHandCandidate.filter { isSameSuit(it, table) }.size > 1) {
                //println("Strategy 5a")
                val card = inHandCandidate.filter { isSameSuit(it, table) }.random()
                return inHand.indexOf(card)
            }

            if (inHandCandidate.filter { isSameRank(it, table) }.size > 1) {
                //println("Strategy 5b")
                val card = inHandCandidate.filter { isSameRank(it, table) }.random()
                return inHand.indexOf(card)
            } else {
                //println("Strategy 5c")
                return inHand.indexOf(inHandCandidate.random())
            }
        }

        return inHand.indices.random()
    }
    fun takeTurn(table: Table, player: Player): Table {
        if (table.isTableEmpty()) {
            println("No cards on the table")
        } else {
            println("${table.elements.size} cards on the table, and the top card is ${table.peek()}")
        }
        println(this.inHand.joinToString(" "))

        val choice = this.chooseStrategy(table)
        if (!table.isTableEmpty() && isMatch(table.peek(), inHand[choice])) {
            println("Computer plays ${inHand[choice]}")
            table.push(inHand[choice])
            inHand.removeAt(choice)
            val cardsFromTable = table.tableClear()
            wonCards.addAll(cardsFromTable)
            println("Computer wins cards")
            this.wonLast = true
            updateScores(player, this)
        } else {
            println("Computer plays ${inHand[choice]}")
            table.push(inHand[choice])
            inHand.removeAt(choice)
        }
        return table
    }


}


fun main() {
    var deck = Deck()
    val player = Player()
    val computer = Computer()


    deck.resetDeck()
    deck.shuffleDeck()
    /*println(ranks.joinToString(" "))
    println(suits.joinToString(" "))
    println(cards.joinToString(" "))*/
    println("Indigo Card Game")
    val firstFlag: Int
    while (true) {
        println("Play first?")
        when (readln().lowercase()) {
            "yes" -> {
                    firstFlag = 1
                    break
            }
            "no" -> {
                firstFlag = 2
                break
            }
            else -> continue
        }
    }
    val initialCards = deck.getCardsSilent(4)
    var table = Table(initialCards)
    println("Initial cards on the table: ${initialCards.joinToString(" ")}")
    deck = player.dealCards(deck)
    deck = computer.dealCards(deck)

    var turnCount = firstFlag
    while(true) {

        if (player.isHandEmpty() && computer.isHandEmpty()) {
            if (deck.isDeckEmpty()) {
                if (table.isTableEmpty()) {
                    println("No cards on the table")
                } else {
                    println("${table.elements.size} cards on the table, and the top card is ${table.peek()}")
                }
                if (player.wonLast) {
                    player.wonCards.addAll(table.elements)
                    table.tableClear()
                } else if (computer.wonLast) {
                    computer.wonCards.addAll(table.elements)
                    table.tableClear()
                } else {
                    if (firstFlag == 1) {
                        player.wonCards.addAll(table.elements)
                        table.tableClear()
                    } else {
                        computer.wonCards.addAll(table.elements)
                        table.tableClear()
                    }
                }
                updateScoresLast(player, computer)
                println("Game Over")
                break
            }
            deck = player.dealCards(deck)
            deck = computer.dealCards(deck)
        }

        if (turnCount % 2 == 1) {
            table = player.takeTurn(table, computer)
            if (player.wonLast) computer.wonLast = false
        } else {
            table = computer.takeTurn(table, player)
            if (computer.wonLast) player.wonLast = false
        }
        turnCount++
    }

}