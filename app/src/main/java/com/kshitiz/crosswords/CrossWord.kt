package com.kshitiz.crosswords

public class Crossword {
    companion object {
        private var across= Array(5) {""}
        private var acrossHint = Array(5) {""}
        private var down = Array(5) {""}
        private var downHint = Array(5) {""}

        private var characters = Array<String>(25) {""}

        private var activeCells = Array<Int>(5) {0}
        private var orientation = "across"

        fun setAcross(arr: Array<String>) {
            this.across = arr
        }
        fun getAcross(): Array<String> {
            return this.across
        }

        fun setAcrossHint(arr: Array<String>) {
            this.acrossHint = arr
        }
        fun getAcrossHint(): Array<String> {
            return this.acrossHint
        }

        fun setDown(arr: Array<String>) {
            this.down = arr
        }
        fun getDown(): Array<String> {
            return this.down
        }

        fun setDownHint(arr: Array<String>) {
            this.downHint = arr
        }
        fun getDownHint(): Array<String> {
            return this.downHint
        }

        fun setCharacters(arr: Array<String>) {
            this.characters = arr
        }
        fun getCharacters(): Array<String> {
            return this.characters
        }

        fun setActiveCells(arr: Array<Int>) {
            this.activeCells = arr
        }
        fun getActiveCells(): Array<Int> {
            return this.activeCells
        }

        fun setOrientation(ort: String) {
            this.orientation = ort
        }
        fun getOrientation(): String {
            return this.orientation
        }

        override
        fun toString(): String {
            return across.toString()+"\n"+ acrossHint.toString()+"\n"+down.toString()+"\n"+ downHint.toString()+"\n"+ characters.toString()
        }

    }
}