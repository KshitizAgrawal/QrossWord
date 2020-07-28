package com.kshitiz.crosswords

import android.app.Dialog
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import org.xmlpull.v1.XmlPullParser
import java.lang.Exception
import android.view.View.OnFocusChangeListener
import android.text.InputType
import android.widget.TextView


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        makeCrossword()
        setboard()
        highlightBox()
//        revealAll()
//        checkBoardComplete()
    }

    /*
        Highlight boxes on board on click
        Highlight whole vertical or horizontal line
        No listener for black boxes
     */
    fun highlightBox() {
        try {

            val characters = Crossword.getCharacters()

            for(i in 0 until 25) {
                val editText = findViewById<EditText>(getResourceId("editText"+i))

                if(characters[i] == "") {
                    editText.inputType = InputType.TYPE_NULL
                    continue
                }

                //works from the second click onwards
                editText.setOnClickListener(View.OnClickListener {
                    highlightLine(i, characters)
                    Toast.makeText(applicationContext, "On click EditText", Toast.LENGTH_LONG).show()
                })
                editText.onFocusChangeListener =
                    OnFocusChangeListener { view, hasFocus ->
                        if (hasFocus) {
                            val color = resources.getColor(R.color.colorPrimary)
                            editText.setBackgroundColor(color)

                            val orientation = Crossword.getOrientation()
                            if (orientation == "down") Crossword.setOrientation("across") else Crossword.setOrientation("down")
                            highlightLine(i, characters)

//                            Toast.makeText(applicationContext, "Got the focus", Toast.LENGTH_LONG)
//                                .show()
                        } else {
                            Toast.makeText(applicationContext, "Lost the focus", Toast.LENGTH_LONG)
                                .show()
                        }
                    }

                editText.addTextChangedListener(object: TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                        Toast.makeText(applicationContext,"executed before making any change over EditText",Toast.LENGTH_SHORT).show()
                    }
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                        Toast.makeText(applicationContext,"executed while making any change over EditText", Toast.LENGTH_SHORT).show()
                    }
                    override fun afterTextChanged(p0: Editable?) {
//                        Toast.makeText(applicationContext,"executed after change made over EditText",Toast.LENGTH_SHORT).show()
                        if(checkBoardComplete()) showPopup()
                    }
                })
            }
        }catch (ex: Exception) {
            Log.e("highlightColor", ex.printStackTrace().toString())
        }
    }

    /*
        highlight the line
     */
    fun highlightLine(index: Int, characters: Array<String>) {

        val current_orientation = Crossword.getOrientation()
        val update_active_cell = ArrayList<Int>()
        val active_cells = Crossword.getActiveCells()

        if(current_orientation.equals("across")) {
            val startIndex = (index%5)

            for(cell in active_cells) {
                if(characters[cell] != "" && cell!=index) {
                    findViewById<EditText>(getResourceId("editText"+cell)).background = getDrawable(R.drawable.cw_box_border)
                }
            }

            for(cell in startIndex until 25 step 5) {
                if(characters[cell] != "" && cell!=index) {
                    val color = resources.getColor(R.color.lightBlue)
                    findViewById<EditText>(getResourceId("editText"+cell)).setBackgroundColor(color)
                }
                update_active_cell.add(cell)
            }

            Crossword.setActiveCells(update_active_cell.toTypedArray())
            Crossword.setOrientation("down")
        }
        else {
            val startIndex = (index/5)*5

            for(cell in active_cells) {
                if(characters[cell] != "" && cell!=index) {
                    findViewById<EditText>(getResourceId("editText"+cell)).background = getDrawable(R.drawable.cw_box_border)
                }
            }

            for(cell in startIndex until startIndex+5) {
                if(characters[cell] != "" && cell!=index) {
                    val color = resources.getColor(R.color.lightBlue)
                    findViewById<EditText>(getResourceId("editText"+cell)).setBackgroundColor(color)
                }
                update_active_cell.add(cell)
            }

            Crossword.setActiveCells(update_active_cell.toTypedArray())
            Crossword.setOrientation("across")
        }
        showHint(index, Crossword.getOrientation())
    }

    /*
        Parse xml crossword
        create crossword object from parsed xml crossword
     */
    fun makeCrossword() {

        var words = ArrayList<String>()
        var hints = ArrayList<String>()
        var characters = ArrayList<String>()
        var text = String()

        try {

            val xpp = getResources().getXml(R.xml.crosswords) as XmlPullParser
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {

                if (xpp.getEventType() == XmlPullParser.TEXT) {
                    text = xpp.text
                    text = text.replace("\"", "")
                }

                else if (xpp.getEventType() == XmlPullParser.END_TAG) {
                    if (xpp.getName().equals("text")) {
                        words.add(text)
                    }
                    else if(xpp.getName().equals("hint")) {
                        hints.add(text)
                    }
                    else if(xpp.getName().equals("character")) {
                        characters.add(text)
                    }
                }

                xpp.next();
            }

            Crossword.setAcross(words.subList(0, 5).toTypedArray())
            Crossword.setAcrossHint(hints.subList(0, 5).toTypedArray())
            Crossword.setDown(words.subList(5, 10).toTypedArray())
            Crossword.setDownHint(hints.subList(5, 10).toTypedArray())
            Crossword.setCharacters(characters.toTypedArray())
            Crossword.setActiveCells(arrayOf(0, 1, 2, 3, 4))
            Crossword.setOrientation("across")
            Log.d("crossword", Crossword.toString())
        }
        catch (ex: Exception) {
            Log.e("Parser", ex.printStackTrace().toString())
        }

    }

    /*
        Set crossword board on start of game
     */
    fun setboard() {
        try {
            var index = 0
            for(character in Crossword.getCharacters()) {
                if (character == "") {
                    val id = getResourceId("editText"+index)
                    val color = resources.getColor(R.color.black)
                    findViewById<EditText>(id).setBackgroundColor(color)
                }
                index++
            }
        } catch (ex: Exception) {
            Log.e("setBoard", ex.printStackTrace().toString())
        }
    }

    /*
        Reveal all words on the board
     */
    fun revealAll() {
        try {
            var index = 0
            for(character in  Crossword.getCharacters()) {
                val id = getResourceId("editText"+index)
                findViewById<EditText>(id).setText(character)
                index++
            }
        }
        catch(ex: Exception) {
            Log.e("Reveal", ex.printStackTrace().toString())
        }
    }

    /*
        check board completion
     */
    fun checkBoardComplete(): Boolean {

        try {
            var index = 0
            for(character in  Crossword.getCharacters()) {
                val id = getResourceId("editText"+index)
                if (findViewById<EditText>(id).text.toString() != character) {
                    return false
                }
                index++
            }
            Toast.makeText(applicationContext,"Board is Complete",Toast.LENGTH_SHORT).show()
            return true
        }
        catch(ex: Exception) {
            Log.e("Reveal", ex.printStackTrace().toString())
        }
        return false
    }

    /*
        show popup upon game completion
     */
    fun showPopup() {

        val myDialog: Dialog = Dialog(this)

        try {
            myDialog.setContentView(R.layout.gameover_popup)
            myDialog.show()
        }
        catch(ex: Exception) {
            Log.e("PopUp", ex.printStackTrace().toString())
        }
    }

    /*
        show hint in the bottom hint box
     */
    fun showHint(wordIndex: Int, orientation: String) {
        if(orientation == "across")
            findViewById<TextView>(R.id.textViewHint).text = Crossword.getAcrossHint()[wordIndex/5]
        else
            findViewById<TextView>(R.id.textViewHint).text = Crossword.getDownHint()[wordIndex%5]
    }

    /*
        create backgrounds for boxes of board for different colors
     */
    fun createDrawable(color: Int): GradientDrawable {
        val gd = GradientDrawable()
        val strokeColor = resources.getColor(R.color.black)
        gd.setStroke(1, strokeColor)
        gd.setColor(color)
//        gd.setPadding(10, 10, 10, 10)
        return gd
    }

    /*
        get editText resource id from their names
     */
    fun getResourceId(res: String): Int{

        when(res) {
            "editText0" -> return R.id.editText0
            "editText1" -> return R.id.editText1
            "editText2" -> return R.id.editText2
            "editText3" -> return R.id.editText3
            "editText4" -> return R.id.editText4
            "editText5" -> return R.id.editText5
            "editText6" -> return R.id.editText6
            "editText7" -> return R.id.editText7
            "editText8" -> return R.id.editText8
            "editText9" -> return R.id.editText9
            "editText10" -> return R.id.editText10
            "editText11" -> return R.id.editText11
            "editText12" -> return R.id.editText12
            "editText13" -> return R.id.editText13
            "editText14" -> return R.id.editText14
            "editText15" -> return R.id.editText15
            "editText16" -> return R.id.editText16
            "editText17" -> return R.id.editText17
            "editText18" -> return R.id.editText18
            "editText19" -> return R.id.editText19
            "editText20" -> return R.id.editText20
            "editText21" -> return R.id.editText21
            "editText22" -> return R.id.editText22
            "editText23" -> return R.id.editText23
            "editText24" -> return R.id.editText24
            else -> return 0
        }
    }
}
