# multiAgents.py
# --------------
# Licensing Information: Please do not distribute or publish solutions to this
# project. You are free to use and extend these projects for educational
# purposes. The Pacman AI projects were developed at UC Berkeley, primarily by
# John DeNero (denero@cs.berkeley.edu) and Dan Klein (klein@cs.berkeley.edu).
# For more info, see http://inst.eecs.berkeley.edu/~cs188/sp09/pacman.html

from util import manhattanDistance, Queue
from game import Directions
import random, util

from game import Agent
import sys
from pacman import GameState


class ReflexAgent(Agent):
  """
    A reflex agent chooses an action at each choice point by examining
    its alternatives via a state evaluation function.

    The code below is provided as a guide.  You are welcome to change
    it in any way you see fit, so long as you don't touch our method
    headers.
  """

  def getAction(self, gameState):
    """
    You do not need to change this method, but you're welcome to.

    getAction chooses among the best options according to the evaluation function.

    Just like in the previous project, getAction takes a GameState and returns
    some Directions.X for some X in the set {North, South, West, East, Stop}
    """
    # Collect legal moves and successor states
    legalMoves = gameState.getLegalActions()

    # Choose one of the best actions
    scores = [self.evaluationFunction(gameState, action) for action in legalMoves]
    bestScore = max(scores)
    bestIndices = [index for index in range(len(scores)) if scores[index] == bestScore]
    chosenIndex = random.choice(bestIndices)  # Pick randomly among the best

    "Add more of your code here if you want to"

    return legalMoves[chosenIndex]

  def evaluationFunction(self, currentGameState, action):
    """
    Design a better evaluation function here.

    The evaluation function takes in the current and proposed successor
    GameStates (pacman.py) and returns a number, where higher numbers are better.

    The code below extracts some useful information from the state, like the
    remaining food (newFood) and Pacman position after moving (newPos).
    newScaredTimes holds the number of moves that each ghost will remain
    scared because of Pacman having eaten a power pellet.

    Print out these variables to see what you're getting, then combine them
    to create a masterful evaluation function.
    """
    # Useful information you can extract from a GameState (pacman.py)
    successorGameState = currentGameState.generatePacmanSuccessor(action)
    newPos = successorGameState.getPacmanPosition()
    newFood = successorGameState.getFood()
    newGhostStates = successorGameState.getGhostStates()
    newScaredTimes = [ghostState.scaredTimer for ghostState in newGhostStates]

    "*** YOUR CODE HERE ***"
    score = 0
    if newFood[newPos[0]][newPos[1]]:
        score = score + 100
    if newPos in successorGameState.getCapsules():
        score = score + 110
    minFoodDistance = 100000
    if currentGameState.getFood().count() == newFood.count():
        for i in newFood.asList():
            minFoodDistance = min(minFoodDistance, manhattanDistance(i, newPos))
        score = score - minFoodDistance
    for i in newGhostStates:
        dist = manhattanDistance(newPos, i.getPosition())
        if dist == 0 and i.scaredTimer == 0 :
            return -99999
        elif dist < 3:
            score = score - (10 - dist) * 10
    return score


def scoreEvaluationFunction(currentGameState):
  """
    This default evaluation function just returns the score of the state.
    The score is the same one displayed in the Pacman GUI.

    This evaluation function is meant for use with adversarial search agents
    (not reflex agents).
  """
  return currentGameState.getScore()


class MultiAgentSearchAgent(Agent):
  """
    This class provides some common elements to all of your
    multi-agent searchers.  Any methods defined here will be available
    to the MinimaxPacmanAgent, AlphaBetaPacmanAgent & ExpectimaxPacmanAgent.

    You *do not* need to make any changes here, but you can if you want to
    add functionality to all your adversarial search agents.  Please do not
    remove anything, however.

    Note: this is an abstract class: one that should not be instantiated.  It's
    only partially specified, and designed to be extended.  Agent (game.py)
    is another abstract class.
  """

  def __init__(self, evalFn='scoreEvaluationFunction', depth='2'):
    self.index = 0  # Pacman is always agent index 0
    self.evaluationFunction = util.lookup(evalFn, globals())
    self.depth = int(depth)


class MinimaxAgent(MultiAgentSearchAgent):
  """
    Your minimax agent (question 2)
  """

  def getAction(self, gameState):
    """
      Returns the minimax action from the current gameState using self.depth
      and self.evaluationFunction.

      Here are some method calls that might be useful when implementing minimax.

      gameState.getLegalActions(agentIndex):
        Returns a list of legal actions for an agent
        agentIndex=0 means Pacman, ghosts are >= 1

      Directions.STOP:
        The stop direction, which is always legal

      gameState.generateSuccessor(agentIndex, action):
        Returns the successor game state after an agent takes an action

      gameState.getNumAgents():
        Returns the total number of agents in the game
    """
    "*** YOUR CODE HERE ***"

    def player_max(gameState, depth):
        if gameState.isWin() or gameState.isLose() or depth == 0:
            return (self.evaluationFunction(gameState), Directions.STOP)
        moves = gameState.getLegalActions(0)
        maxvalue = float('-inf')
        action = Directions.STOP
        for move in moves:
            if move != Directions.STOP:
                newState = gameState.generatePacmanSuccessor(move)
                newvalue = ghost_min(newState, depth, 1)
                if maxvalue <= newvalue:
                    maxvalue = newvalue
                    action = move
        return (maxvalue, action)

    def ghost_min(gameState, depth, ghost_index):
        if gameState.isWin() or gameState.isLose() or depth == 0:
            return self.evaluationFunction(gameState)
        ghost_number = gameState.getNumAgents() - 1
        moves = gameState.getLegalActions(ghost_index)
        minvalue = float('inf')
        for move in moves:
                newState = gameState.generateSuccessor(ghost_index, move)
                if ghost_number > ghost_index:
                    minvalue = min(minvalue, ghost_min(newState, depth, ghost_index + 1))
                else:
                    minvalue = min(minvalue, player_max(newState, depth - 1)[0])
        return minvalue

    return player_max(gameState, self.depth)[1]
      

class AlphaBetaAgent(MultiAgentSearchAgent):
  """
    Your minimax agent with alpha-beta pruning (question 3)
  """

  def getAction(self, gameState):
    """
      Returns the minimax action using self.depth and self.evaluationFunction
    """
    "*** YOUR CODE HERE ***"

    def player_max(gameState, depth, alpha, beta):
        if gameState.isWin() or gameState.isLose() or depth == 0:
            return (self.evaluationFunction(gameState), Directions.STOP)
        moves = gameState.getLegalActions(0)
        maxvalue = float('-inf')
        action = Directions.STOP
        for move in moves:
            if move != Directions.STOP:
                newState = gameState.generatePacmanSuccessor(move)
                newvalue = ghost_min(newState, depth, 1, alpha, beta)
                if maxvalue <= newvalue:
                    maxvalue = newvalue
                    action = move
                    if maxvalue >= beta:
                        return (maxvalue, action)
                alpha = max(alpha, maxvalue)
        return (maxvalue, action)

    def ghost_min(gameState, depth, ghost_index, alpha, beta):
        if gameState.isWin() or gameState.isLose() or depth == 0:
            return self.evaluationFunction(gameState)
        ghost_number = gameState.getNumAgents() - 1
        moves = gameState.getLegalActions(ghost_index)
        minvalue = float('inf')
        for move in moves:
                newState = gameState.generateSuccessor(ghost_index, move)
                if ghost_number > ghost_index:
                    minvalue = min(minvalue, ghost_min(newState, depth, ghost_index + 1, alpha, beta))
                else:
                    minvalue = min(minvalue, player_max(newState, depth - 1, alpha, beta)[0])
                if minvalue <= alpha:
                    return minvalue
                beta = min(beta, minvalue)
        return minvalue

    return player_max(gameState, self.depth, float('-inf'), float('inf'))[1]


class ExpectimaxAgent(MultiAgentSearchAgent):
  """
    Your expectimax agent (question 4)
  """

  def getAction(self, gameState):
    """
      Returns the expectimax action using self.depth and self.evaluationFunction

      All ghosts should be modeled as choosing uniformly at random from their
      legal moves.
    """
    "*** YOUR CODE HERE ***"

    def player_max(gameState, depth):
        if gameState.isWin() or gameState.isLose() or depth == 0:
            return (self.evaluationFunction(gameState), Directions.STOP)
        moves = gameState.getLegalActions(0)
        maxvalue = float('-inf')
        action = Directions.STOP
        for move in moves:
            if move != Directions.STOP:
                newState = gameState.generatePacmanSuccessor(move)
                newvalue = ghost_minavg(newState, depth, 1)
                if maxvalue <= newvalue:
                    maxvalue = newvalue
                    action = move
        return (maxvalue, action)

    def ghost_minavg(gameState, depth, ghost_index):
        if gameState.isWin() or gameState.isLose() or depth == 0:
            return self.evaluationFunction(gameState)
        ghost_number = gameState.getNumAgents() - 1
        moves = gameState.getLegalActions(ghost_index)
        minvalue = float('inf')
        total = 0;
        for move in moves:
            newState = gameState.generateSuccessor(ghost_index, move)
            if ghost_number > ghost_index:
                total = total + ghost_minavg(newState, depth, ghost_index + 1)
            else:
                total = total + player_max(newState, depth - 1)[0]
        return total / len(moves)

    return player_max(gameState, self.depth)[1]


def betterEvaluationFunction(currentGameState):
    """
    Your extreme ghost-hunting, pellet-nabbing, food-gobbling, unstoppable
    evaluation function (question 5).

    DESCRIPTION: Check the two base cases (win and lose) first. Then use bfs to get the actural 
    distance to the nearest food to to be more accuracy than using manhattanDistance (commented). 
    (Using manhattan give me a little lower than 1000 while running) The other parts I mainly copied
    from my reflexion part. I also gave it a large negative * the number of food left to encourage it to
    eat. I checked the nearest ghost. If it is too close, it would not be a good position to go unless it
    is in its scare time.
    
    """
    "*** YOUR CODE HERE ***"
    if currentGameState.isWin():
        return float('inf')
    if currentGameState.isLose():
        return float('-inf')
    pos = currentGameState.getPacmanPosition()
    food = currentGameState.getFood()
    ghostStates = currentGameState.getGhostStates()
    
    score = -100 * food.count() - 10 * len(currentGameState.getCapsules())
    
#     minFoodDistance = float('inf')
#     for i in food.asList():
#         minFoodDistance = min(minFoodDistance, manhattanDistance(i, pos))
#     score = score - minFoodDistance

    walls = currentGameState.getWalls()
    distanceMap = walls.copy()
    s = Queue()
    s.push(pos)
    distanceMap[pos[0]][pos[1]] = 0
    distance = 0
    while not s.isEmpty():
        x , y = s.pop()
        distance = distanceMap[x][y] + 1
        if food[x][y]:
            break
        else:
            if distanceMap[x][y + 1] == False:
                distanceMap[x][y + 1] = distance
                s.push((x, y + 1))
            if distanceMap[x][y - 1] == False:
                distanceMap[x][y - 1] = distance
                s.push((x, y - 1))
            if distanceMap[x + 1][y] == False:
                distanceMap[x + 1][y] = distance
                s.push((x + 1, y))
            if distanceMap[x - 1][y] == False:
                distanceMap[x - 1][y] = distance
                s.push((x - 1, y))
    score = score - distance
    
    minGhostDistance = float('inf')
    for i in ghostStates:
        if i.scaredTimer == 0:
            minGhostDistance = min(minGhostDistance, manhattanDistance(pos, i.getPosition()))
    if minGhostDistance < 5:
        score = score - 10 ** (10 - minGhostDistance) 
    return score


# Abbreviation
better = betterEvaluationFunction


class ContestAgent(MultiAgentSearchAgent):
  """
    Your agent for the mini-contest
  """

  def getAction(self, gameState):
    """
      Returns an action.  You can use any method you want and search to any depth you want.
      Just remember that the mini-contest is timed, so you have to trade off speed and computation.

      Ghosts don't behave randomly anymore, but they aren't perfect either -- they'll usually
      just make a beeline straight towards Pacman (or away from him if they're scared!)
    """
    "*** YOUR CODE HERE ***"
    def __init__(self, evalFn='betterEvaluationFunction', depth='2'):
      self.index = 0  # Pacman is always agent index 0
      self.evaluationFunction = util.lookup(evalFn, globals())
      self.depth = int(depth)

