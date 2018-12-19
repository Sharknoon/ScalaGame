package controller

import model.{Game, Player, Token}
import scalafx.beans.property.ObjectProperty

import scala.collection.mutable

class GameController(game: Game) {

  private val activePlayer = new ObjectProperty[Player]() {
    value = game.players._1
  }

  /**
    * Change the active player in case that the turn of the current player
    * is over
    */
  def changePlayer(): Player = {
    activePlayer.set(if (getActivePlayer == game.players._1) game.players._2 else game.players._1)
    getActivePlayer
  }

  /**
    * Returns the current active player
    */
  def getActivePlayer: Player = {
    activePlayer.get()
  }

  def activePlayerProperty: ObjectProperty[Player] = {
    activePlayer
  }

  /**
    * Returns the current active game
    */
  def getGame: Game = {
    game
  }

  /**
    * Checks if a player has already set all his tokens or not
    * at the beginning of the game
    */
  def canSetTokens: Boolean = {
    getActivePlayer.unsetTokens.get() > 0
  }

  /**
    * Set a token of a player to the playground at the beginning of the game
    * Each player has 9 tokens to set
    */
  def setToken(position: (Int, Int)): Unit = {
    val newToken = Token(getActivePlayer)
    game.playground.fields(position).set(newToken)
    getActivePlayer.unsetTokens.value -= 1
    getActivePlayer.tokensInGame.value += 1
  }

  /**
    * Checks if the position where the token of the player should be set is free
    * or already occupied by another token
    * In case that no token occupied on the field, the object in the object property is null
    */
  def isPositionFree(position: (Int, Int)): Boolean = {
    val field: ObjectProperty[Token] = game.playground.fields(position)
    field.get() == null
  }

  /**
    * Checks if the active player gets 3 tokens in a row after he sets or move a token
    * In this case the active player is allowed to remove one token from the other player
    */
  def checkForThreeInARow(position: (Int, Int)): Boolean = {

    // Corner field
    // New position of the moved token is in a corner (all odd fields)
    if (mod(position._2, 2) == 1) {

      // Check two previous tokens
      // Exception field 1 => in this case the previous token is 8
      // In all other case the previous tokens can be calculate by modulo 8
      val firstPredecessor = getRingField(position._2, _ - 1)

      // The previous token has the same color as the active player,
      // check the second previous token
      if (isPositionSetByCurrentPlayer((position._1, firstPredecessor))) {
        // Check two previous token
        // Exception field 1 => in this case the previous token is 8
        // In all other case the previous tokens can be calculate by modulo 8
        val secondPredecessor = getRingField(position._2, _ - 2)

        // The second previous token has also the same color as the active player
        // 3 same color tokens are in one row
        if (isPositionSetByCurrentPlayer((position._1, secondPredecessor))) {
          return true
        }
      }

      // Check for two successor tokens
      val firstSuccessor = getRingField(position._2, _ + 1)

      // The successor token has the same color as the active player,
      // check the second successor token
      if (isPositionSetByCurrentPlayer((position._1, firstSuccessor))) {
        // Check two previous token
        // Exception field 1 => in this case the previous token is 8
        // In all other case the previous tokens can be calculate by modulo 8
        val secondSuccessor = getRingField(position._2, _ + 2)

        // The second successor token has also the same color as the active player
        // 3 same color tokens are in one row
        if (isPositionSetByCurrentPlayer((position._1, secondSuccessor))) {
          return true
        }
      }

      // Middle field
      // New position of the moved token is in the middle (all even fields)
    } else {

      //The left neighbour of the token in the ring
      val left = getRingField(position._2, _ - 1)

      //Check for the left neighbour, only then the check for the right neighbour is necessary
      if (isPositionSetByCurrentPlayer((position._1, left))) {

        //The right neighbour of the token in the ring
        val right = getRingField(position._2, _ + 1)

        //This means that both neighbours have the same color as this token
        if (isPositionSetByCurrentPlayer((position._1, right))) {
          return true
        }
      }

      //Check for 3 same colors of the tokens between the rings
      //These are the rings to be checked against
      val positionsToCheck = position._1 match {
        case 1 => (2, 3)
        case 2 => (1, 3)
        case 3 => (1, 2)
        //shouldn't happen
        case _ => (-1, -1)
      }

      //Check the first ring
      if (isPositionSetByCurrentPlayer((positionsToCheck._1, position._2))) {

        //Check second ring
        if (isPositionSetByCurrentPlayer((positionsToCheck._2, position._2))) {
          return true
        }
      }

    }
    false
  }

  /**
    *
    * @param field    The field (index inside the ring)
    * @param modifier The modifier of the field (e.g. 1 for the successor or -2 for the pre-predecessor)
    * @return the index of the token of the requested field item (before 1 is 8)
    */
  private def getRingField(field: Int, modifier: Int => Int): Int = {
    //We dont have a 0 index so we must handle this special case
    if (mod(modifier.apply(field), 8) == 0) {
      8
    } else {
      mod(modifier.apply(field), 8)
    }
  }

  /**
    * Checks if the token of the hand over position has the same color as the active player
    */
  def isPositionSetByCurrentPlayer(position: (Int, Int)): Boolean = {
    val token = game.playground.fields(position).get()
    if (token == null) {
      return false
    }
    token.player == getActivePlayer
  }

  /**
    * Moves a token
    *
    * @param currentPosition the current position of the token
    * @param toMovePosition  the destination of the new token
    * @return true if allowed and successful, false otherwise
    */
  def moveToken(currentPosition: (Int, Int), toMovePosition: (Int, Int)): Boolean = {

    // Position is already occupied by another token
    if (!isPositionFree(toMovePosition)) {
      return false
    }

    // Get successor and predecessor position
    val successor = (currentPosition._1, getRingField(currentPosition._2, _ + 1))
    val predecessor = (currentPosition._1, getRingField(currentPosition._2, _ - 1))

    // Either successor or predecessor is equal to move position
    // Change token in playground
    if (toMovePosition == successor || toMovePosition == predecessor) {
      val currentPositionToken = game.playground.fields(currentPosition).get()
      game.playground.fields(currentPosition).set(null)
      game.playground.fields(toMovePosition).set(currentPositionToken)
      return true
    }

    // Check for middle
    if ((currentPosition._2 % 2) == 0) {

      var firstRingPosition = (-1, -1)
      var secondRingPosition = (-1, -1)
      // Check the ring
      currentPosition._1 match {
        // Only move to ring 2
        case 1 => firstRingPosition = (2, currentPosition._2)
        case 2 =>
          firstRingPosition = (1, currentPosition._2)
          secondRingPosition = (3, currentPosition._2)
        case 3 => firstRingPosition = (2, currentPosition._2)
      }

      if (toMovePosition == firstRingPosition || toMovePosition == secondRingPosition) {
        val currentPositionToken = game.playground.fields(currentPosition).get()
        game.playground.fields(currentPosition).set(null)
        game.playground.fields(toMovePosition).set(currentPositionToken)
        return true
      }
    }
    false
  }

  /**
    * Deletes the token of the opponent player at the specified position
    */
  def deleteOpponentToken(position: (Int, Int)): Boolean = {

    val positionToken = game.playground.fields(position).get()

    // Position has no token of opponent player or the token is protected
    if (positionToken == null ||
      positionToken.player == getActivePlayer ||
      checkForThreeInARow(position)) {
      false
    } else {
      // Delete opponent token
      game.playground.fields(position).set(null)
      true
    }
  }

  /**
    * Checks if a player isn't able to move a token or he has only two tokens left
    *
    * @return the winner
    */
  def isGameOver: Option[Player] = {
    val player1 = game.players._1
    val player2 = game.players._2

    //Check if there are only two tokens left, if so, return the winner
    if (player1.tokensInGame.get() < 3) {
      return Option(player2)
    }
    if (player2.tokensInGame.get() < 3) {
      return Option(player1)
    }

    //map for storing the amount of non movable tokens to check against all tokens
    //of the player in the playground
    val canPlayerMove = new mutable.HashMap[Player, Int]()
    canPlayerMove.put(player1, 0)
    canPlayerMove.put(player2, 0)

    //All tokens of the game
    val tokens = game.playground.fields.filter(e => e._2.value != null)
    for (token <- tokens) {
      //For every token in the playground
      val playerOfTheToken = token._2.get().player
      //Check if a player cant move
      if (!canMove(token._1)) {
        //if the player cant move, increase the counter
        canPlayerMove(playerOfTheToken) += 1
      }
    }

    //If the amount of the non movable tokens equals the amount of tokens the player
    //has on the field, then he has lost (opposite has won)
    if (canPlayerMove(player1) >= player1.tokensInGame.get()) {
      return Option(player2)
    }

    if (canPlayerMove(player2) >= player2.tokensInGame.get()) {
      return Option(player1)
    }

    //game can continue
    Option.empty
  }

  /**
    * Checks whether a token can move or is buried
    *
    * @param token The token to be checked against
    * @return True if this token is able to move, false otherwise
    */
  private def canMove(token: (Int, Int)): Boolean = {
    val ringFieldPredecessor = (token._1, getRingField(token._2, _ - 1))
    val ringFieldSuccessor = (token._1, getRingField(token._2, _ + 1))

    //corner token
    if (token._2 % 2 == 1) {
      return isPositionFree(ringFieldPredecessor) ||
        isPositionFree(ringFieldSuccessor)
    } else {
      //middle point

      var firstRingPosition = (-1, -1)
      var secondRingPosition = (-1, -1)

      // Check the ring
      token._1 match {
        // Only possible to ring 2
        case 1 => firstRingPosition = (2, token._2)
        case 2 =>
          firstRingPosition = (1, token._2)
          secondRingPosition = (3, token._2)
        case 3 => firstRingPosition = (2, token._2)
      }

      if (secondRingPosition._1 > 0) {
        return isPositionFree(ringFieldPredecessor) ||
          isPositionFree(ringFieldSuccessor) ||
          isPositionFree(firstRingPosition) ||
          isPositionFree(secondRingPosition)
      } else {
        return isPositionFree(ringFieldPredecessor) ||
          isPositionFree(ringFieldSuccessor) ||
          isPositionFree(firstRingPosition)
      }
    }

    true
  }
}
