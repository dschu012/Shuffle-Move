/*  ShuffleMove - A program for identifying and simulating ideal moves in the game
 *  called Pokemon Shuffle.
 *  
 *  Copyright (C) 2015  Andrew Meyers
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package shuffle.fwk.data.simulation;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import shuffle.fwk.data.Board;
import shuffle.fwk.data.simulation.util.NumberSpan;

/**
 * @author Andrew Meyers
 *
 */
public class SimulationResult {
   
   private final List<Integer> move;
   private final Board board;
   private final NumberSpan score;
   private final NumberSpan gold;
   private final NumberSpan megaProgress;
   private final UUID processUUID;
   private final NumberSpan numBlocksCleared;
   private final NumberSpan numDisruptionsCleared;
   private final NumberSpan numCombosCleared;
   private NumberSpan rightSideGoldScore;
   private int hash;
   private int moveNumber = 0;
   private final long startTime;
   private SimulationResult parent = null;

   private Collection<SimulationResult> children = new LinkedList<>();
   
   public SimulationResult(List<Integer> sourceMove, Board resultBoard, NumberSpan givenScore, NumberSpan givenGold,
         NumberSpan progress, UUID id, NumberSpan blocksCleared, NumberSpan disruptionsCleared,
         NumberSpan combosCleared, NumberSpan givenRightSideGoldScore, long startTime) {
      if (resultBoard == null) {
         throw new NullPointerException("Cannot create a SimulaitonResult with a null board.");
      }
      numBlocksCleared = blocksCleared.clone();
      numDisruptionsCleared = disruptionsCleared.clone();
      numCombosCleared = combosCleared.clone();
      score = givenScore.clone();
      gold = givenGold.clone();
      rightSideGoldScore = givenRightSideGoldScore.clone();
      megaProgress = progress.clone();
      processUUID = id;
      move = sourceMove;
      board = resultBoard;
      this.startTime = startTime;
      hash = generateHash();
   }
   
   /**
    * @return
    */
   public int generateHash() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (board == null ? 0 : board.hashCode());
      result = prime * result + (move == null ? 0 : move.hashCode());
      result = prime * result + (numBlocksCleared == null ? 0 : numBlocksCleared.hashCode());
      result = prime * result + (numDisruptionsCleared == null ? 0 : numDisruptionsCleared.hashCode());
      result = prime * result + (numCombosCleared == null ? 0 : numCombosCleared.hashCode());
      result = prime * result + (score == null ? 0 : score.hashCode());
      result = prime * result + (gold == null ? 0 : gold.hashCode());
      result = prime * result + (rightSideGoldScore == null ? 0 : rightSideGoldScore.hashCode());
      result = prime * result + (megaProgress == null ? 0 : megaProgress.hashCode());
      result = prime * result + (parent == null ? 0 : parent.hashCode());
      result = prime * result + (children == null ? 0 : children.hashCode());
      result = prime * result + (int) (moveNumber ^ moveNumber >> 32);
      result = prime * result + (int) (startTime ^ startTime >> 32);
      this.hash = result;
      return result;
   }
   
   public UUID getID() {
      return processUUID;
   }
   
   public long getStartTime() {
      return startTime;
   }
   
   public List<Integer> getMove() {
      List<Integer> ret = Collections.emptyList();
      if (move != null) {
         ret = Collections.unmodifiableList(move);
      }
      return ret;
   }
   
   public Board getBoard() {
      return board;
   }
   
   public NumberSpan getNetScore() {
      return score;
   }
   
   public NumberSpan getNetGold() {
      return gold;
   }

   public NumberSpan getRightSideGoldScore() {
      return rightSideGoldScore;
   }

   public void setRightSideGoldScore(NumberSpan rightSideGoldScore) {
      this.rightSideGoldScore =  rightSideGoldScore;
   }

   public void addRightSideGoldScore(NumberSpan rightSideGoldScore) {
      this.rightSideGoldScore = this.rightSideGoldScore.add(rightSideGoldScore);
   }

   public SimulationResult getMaxRightSideGoldScore() {
      return flatten(this)
         .max(Comparator.comparing(SimulationResult::getRightSideGoldScore))
         .get();
   }

   public static Stream<SimulationResult> flatten(SimulationResult node) {
      return node.getChildren().stream()
               .flatMap(n -> {
                  if(n.getChildren().isEmpty()) {
                     return Stream.of(n);
                  }
                  return flatten(n);
               });
   }

   public void setChildren(Collection<SimulationResult> children) {
      this.children = children;
   }

   public Collection<SimulationResult> getChildren() {
      return this.children;
   }

   public SimulationResult getParent() {
      return parent;
   }

   public void setMoveNumber(int moveNumber) {
      this.moveNumber = moveNumber;
   }

   public int getMoveNumber() {
      return this.moveNumber;
   }

   public void setParent(SimulationResult parent) {
      this.parent = parent;
   }

   public SimulationResult getRoot() {
      if(this.parent == null) {
         return this;
      }
      return this.parent.getRoot();
   }
   
   public NumberSpan getBlocksCleared() {
      return numBlocksCleared;
   }
   
   public NumberSpan getDisruptionsCleared() {
      return numDisruptionsCleared;
   }
   
   public NumberSpan getCombosCleared() {
      return numCombosCleared;
   }
   
   public NumberSpan getProgress() {
      return megaProgress;
   }
   
   @Override
   public String toString() {
      int row1 = 0;
      int row2 = 0;
      int column1 = 0;
      int column2 = 0;
      if (move != null && move.size() >= 2) {
         row1 = move.get(0);
         column1 = move.get(1);
         if (move.size() >= 4) {
            row2 = move.get(2);
            column2 = move.get(3);
         }
      }
      String s = String.format("%s,%s -> %s,%s: %sg, %s score, %s combos, %s blocks, %s disruptions, %s mega progress, %s right side gold score",
         row1, column1, row2, column2, gold, score, numCombosCleared, numBlocksCleared, numDisruptionsCleared,
         megaProgress, rightSideGoldScore);
      return parent != null ? parent.toString() + '\n' + s : s;
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      return hash;
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      boolean equal = obj != null && obj instanceof SimulationResult;
      if (equal) {
         SimulationResult other = (SimulationResult) obj;
         equal &= move == other.move || move != null && move.equals(other.move);
         equal &= board == other.board || board != null && board.equals(other.board);
         equal &= score == other.score || score != null && score.equals(other.score);
         equal &= megaProgress == other.megaProgress || megaProgress != null && megaProgress.equals(other.megaProgress);
         equal &= numBlocksCleared == other.numBlocksCleared || numBlocksCleared != null
               && numBlocksCleared.equals(other.numBlocksCleared);
         equal &= numDisruptionsCleared == other.numDisruptionsCleared || numDisruptionsCleared != null
               && numDisruptionsCleared.equals(other.numDisruptionsCleared);
         equal &= numCombosCleared == other.numCombosCleared || numCombosCleared != null
               && numCombosCleared.equals(other.numCombosCleared);
         equal &= parent.equals(other.parent);
         equal &= children.equals(other.children);
         equal &= moveNumber == other.moveNumber;
         equal &= startTime == other.startTime;
      }
      return equal;
   }
}
