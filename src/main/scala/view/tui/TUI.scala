package view.tui

import controller.{GameController, MenuController}
import model.{Color, Playground, StringConstants}

class TUI {

  setStartMenuInput()

  def setStartMenuInput(): Unit = {

    println(StringConstants.ASK_FOR_PLAYER_NAMES)
    println(StringConstants.PLAYER1)
    val player1 = scala.io.StdIn.readLine()
    println(StringConstants.PLAYER2)
    val player2 = scala.io.StdIn.readLine()

    // Empty names
    if (player1.isEmpty || player2.isEmpty) {
      println(StringConstants.EMPTY_PLAYER_NAMES)
      setStartMenuInput()
      return
    }

    // Start new game
    val menuController = new MenuController((player1, player2))
    val gameController = menuController.startNewGame()
    changeTurn(gameController)

  }

  /**
    * Starts new turn for the next player
    */
  def changeTurn(gameController: GameController): Unit = {

    val activePlayer = gameController.getActivePlayer
    println(StringConstants.ACTIVE_PLAYER + activePlayer.name + " (" + activePlayer.color + ") " + StringConstants.ACTIVE_PLAYER_IS_ON_TURN)

    // Print out the playground layout with the Ids of each field and
    // the current playground with all set tokens
    printPlayGroundLayout()
    printCurrentPlayGround(gameController.getGame.playground)

    // Check if active player has to set one of his 9 tokens
    // If yes, he has to set a token
    // If no, he has to move or jump (if allowed) with a token
    if (gameController.canSetTokens(activePlayer)) {
      setToken(gameController)
    } else {
      moveToken(gameController)
    }
  }

  /**
    * Prints out the playground with all field Ids on the terminal
    */
  def printPlayGroundLayout(): Unit = {
    println(
      "11--------12---------13\n" +
        "|          |          |\n" +
        "|   21----22-----23   |\n" +
        "|   |      |      |   |\n" +
        "|   |  31-32-33   |   |\n" +
        "|   |   |     |   |   |\n" +
        "18--28-38    34--24--14\n" +
        "|   |   |     |   |   |\n" +
        "|   |  37-36-35   |   |\n" +
        "|   |      |      |   |\n" +
        "|   27----26-----25   |\n" +
        "|          |          |\n" +
        "17--------16---------15")
  }

  /**
    * Prints out the current playground with all set tokens of the players
    * B stands for all black tokens
    * W stands for all white tokens
    */
  def printCurrentPlayGround(playground: Playground): Unit = {
    val oneOne = getFieldToken(playground, (1, 1))
    val oneTwo = getFieldToken(playground, (1, 2))
    val oneThree = getFieldToken(playground, (1, 3))
    val oneFour = getFieldToken(playground, (1, 4))
    val oneFive = getFieldToken(playground, (1, 5))
    val oneSix = getFieldToken(playground, (1, 6))
    val oneSeven = getFieldToken(playground, (1, 7))
    val oneEight = getFieldToken(playground, (1, 8))
    val twoOne = getFieldToken(playground, (2, 1))
    val twoTwo = getFieldToken(playground, (2, 2))
    val twoThree = getFieldToken(playground, (2, 3))
    val twoFour = getFieldToken(playground, (2, 4))
    val twoFive = getFieldToken(playground, (2, 5))
    val twoSix = getFieldToken(playground, (2, 6))
    val twoSeven = getFieldToken(playground, (2, 7))
    val twoEight = getFieldToken(playground, (2, 8))
    val threeOne = getFieldToken(playground, (3, 1))
    val threeTwo = getFieldToken(playground, (3, 2))
    val threeThree = getFieldToken(playground, (3, 3))
    val threeFour = getFieldToken(playground, (3, 4))
    val threeFive = getFieldToken(playground, (3, 5))
    val threeSix = getFieldToken(playground, (3, 6))
    val threeSeven = getFieldToken(playground, (3, 7))
    val threeEight = getFieldToken(playground, (3, 8))

    println(
      s"$oneOne----------$oneTwo----------$oneThree\n" +
        "|          |          |\n" +
        s"|   $twoOne------$twoTwo------$twoThree   |\n" +
        "|   |      |      |   |\n" +
        s"|   |   $threeOne--$threeTwo--$threeThree   |   |\n" +
        "|   |   |     |   |   |\n" +
        s"$oneEight---$twoEight---$threeEight     $threeFour---$twoFour---$oneFour\n" +
        "|   |   |     |   |   |\n" +
        s"|   |   $threeSeven--$threeSix--$threeFive   |   |\n" +
        "|   |      |      |   |\n" +
        s"|   $twoSeven------$twoSix------$twoFive   |\n" +
        "|          |          |\n" +
        s"$oneSeven----------$oneSix----------$oneFive")
  }

  /**
    * Gets the value of the field
    * B for a black token
    * W for a white token
    * . for a free field
    */
  def getFieldToken(playground: Playground, field: (Int, Int)): String = {
    val blackToken = "B"
    val whiteToken = "W"
    val noToken = "."

    // Get the field value
    val fieldValue = playground.fields(field).get()

    if (fieldValue == null) {
      noToken
    } else if (fieldValue.player.color == Color.WHITE) {
      whiteToken
    } else {
      blackToken
    }
  }

  /**
    * Ask active player for setting a token
    */
  def setToken(gameController: GameController): Unit = {
    // Get input of the token position
    println(StringConstants.SET_TOKEN)
    val positionInput = scala.io.StdIn.readLine()

    // Check that all chars of the input string are numbers
    if (!isAllDigits(positionInput)) {
      println(StringConstants.SET_TOKEN_NOT_NUMERIC)
      setToken(gameController)
      return
    }

    val ring = positionInput.charAt(0)
    val field = positionInput.charAt(1)
    val position = (ring.toString.toInt, field.toString.toInt)

    // check if the position is still free
    if (!gameController.isPositionFree(position)) {
      println(StringConstants.SET_TOKEN_NO_FREE_POSITION)
      setToken(gameController)
      return
    }

    // Set token to position
    gameController.setToken(position)
  }

  /**
    * Checks if a string contains always numbers
    */
  def isAllDigits(x: String): Boolean = x forall Character.isDigit

  /**
    * Move token
    */
  def moveToken(gameController: GameController): Unit = {

  }
}
