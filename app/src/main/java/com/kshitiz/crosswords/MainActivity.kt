package com.kshitiz.crosswords

import androidx.appcompat.app.AppCompatActivity
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.*
import java.lang.Exception
import org.xmlpull.v1.XmlPullParser
import android.widget.TextView
import android.os.Handler


class MainActivity : AppCompatActivity() {

    private var pack: String? = "daily"
    private var packLevel: Int = 0

    private var activeBox: Int = 0
    private var finalTime: String = "00:00"
    private var finalScore: Int = 0

    lateinit var timerTextView: TextView
    var startTime: Long = 0

    //runs without a timer by reposting this handler at the end of the runnable
    var timerHandler = Handler()
    var timerRunnable: Runnable = object : Runnable {

        override fun run() {
            val millis = System.currentTimeMillis() - startTime
            var seconds = (millis / 1000).toInt()
            val minutes = seconds / 60
            seconds = seconds % 60

            timerTextView.text = String.format("%d:%02d", minutes, seconds)

            timerHandler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            pack = intent.getStringExtra("pack")
            if(pack == "daily")
                loadCrossword(getResourceId("$pack"))
            else {
                packLevel = intent.getIntExtra("packLevel", 0)
                loadCrossword(getResourceId("$pack$packLevel"))
            }
            setupBoard()
        } catch(ex: Exception) {
            Log.e("MainActivity", "Unable to Start Activity")
        }
    }

    /*
        go to either homepage or pack selection page on back press button
     */
    override fun onBackPressed() {
        if(pack == "daily") {
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
        }
        else {
            val intent = Intent(this, CrossWordSeriesActivity::class.java)
            startActivity(intent)
        }
    }

    /*
        Highlight boxes on board on click
        Highlight whole vertical or horizontal line
        No listener for black boxes
     */
    fun setupBoard() {
        try {

            val characters = Crossword.getCharacters()

            for(i in 0 until 25) {
                val editText = findViewById<EditText>(getResourceId("editText"+i))

                editText.inputType = InputType.TYPE_NULL

                if(characters[i] == "") {
                    val color = resources.getColor(R.color.black)
                    editText.setBackgroundColor(color)
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

                            activeBox = i
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
                        editText.setTextColor(resources.getColor(R.color.black))
                        if(checkBoardComplete()) {
                            setupClock("stop")
                            showPopup()
                        }
                        checkWord()
                    }
                })
            }

            timerTextView = findViewById(R.id.txtViewClock)
            setupClock("start")
            setOnClickListeners()
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
                    findViewById<EditText>(getResourceId("editText"+cell)).background = getDrawable(R.drawable.cell_rect_border)
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
                    findViewById<EditText>(getResourceId("editText"+cell)).background = getDrawable(R.drawable.cell_rect_border)
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
        highlight line after complete
     */

    fun highlightLineComplete() {
        val activeCells = Crossword.getActiveCells()
        for (cell in activeCells) {
            val editText = findViewById<EditText>(getResourceId("editText$cell"))
            if(editText.text.toString() != "") {
                editText.background = getDrawable(R.drawable.cell_circular_background)
            }
        }
        Toast.makeText(applicationContext,"This word is correct",Toast.LENGTH_SHORT).show()
    }

    /*
        Parse xml crossword
        create crossword object from parsed xml crossword
     */
    fun loadCrossword(boardName: Int) {

        try {

            val words = ArrayList<String>()
            val hints = ArrayList<String>()
            val characters = ArrayList<String>()
            var text = String()

            val xpp = getResources().getXml(boardName)

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
        reveal the current word
     */
    fun revealWord() {
        val characters = Crossword.getCharacters()
        val activeCells = Crossword.getActiveCells()

        for(cell in activeCells) {
            if(characters[cell] != "") {
                findViewById<EditText>(getResourceId("editText$cell")).text = Editable.Factory.getInstance().newEditable(characters[cell])
            }
        }
    }

    /*
        Reveal all words on the board
     */
    fun revealAll() {
        try {
            var cell = 0
            for(character in  Crossword.getCharacters()) {
                findViewById<EditText>(getResourceId("editText$cell")).text = Editable.Factory.getInstance().newEditable(character)
                cell++
            }
        }
        catch(ex: Exception) {
            Log.e("Reveal", ex.printStackTrace().toString())
        }
    }

    /*
        check the current word
    */
    fun checkWord(user: Boolean = false) {
        val characters = Crossword.getCharacters()
        val activeCells = Crossword.getActiveCells()

        var isCorrect = true

        for(cell in activeCells) {
            val editText = findViewById<EditText>(getResourceId("editText$cell"))
            if(characters[cell] != editText.text.toString()) {
                isCorrect = false
                if (user == true && "" != editText.text.toString())
                    editText.setTextColor(resources.getColor(R.color.colorAccent))
            }
        }

        if(isCorrect == true) {
            highlightLineComplete()
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
        on click listeners on custom keyboard keys
     */
    fun setOnClickListeners() {

//        findViewById<Button>(R.id.btnClue).setOnClickListener{TODO("implement clues for each word")}
        findViewById<Button>(R.id.btnCheck).setOnClickListener{checkWord(true)}
        findViewById<Button>(R.id.btnReveal).setOnClickListener{revealWord()}

        findViewById<Button>(R.id.btnKeyA).setOnClickListener{changeText("A")}
        findViewById<Button>(R.id.btnKeyB).setOnClickListener{changeText("B")}
        findViewById<Button>(R.id.btnKeyC).setOnClickListener{changeText("C")}
        findViewById<Button>(R.id.btnKeyD).setOnClickListener{changeText("D")}
        findViewById<Button>(R.id.btnKeyE).setOnClickListener{changeText("E")}
        findViewById<Button>(R.id.btnKeyF).setOnClickListener{changeText("F")}
        findViewById<Button>(R.id.btnKeyG).setOnClickListener{changeText("G")}
        findViewById<Button>(R.id.btnKeyH).setOnClickListener{changeText("H")}
        findViewById<Button>(R.id.btnKeyI).setOnClickListener{changeText("I")}
        findViewById<Button>(R.id.btnKeyJ).setOnClickListener{changeText("J")}
        findViewById<Button>(R.id.btnKeyK).setOnClickListener{changeText("K")}
        findViewById<Button>(R.id.btnKeyL).setOnClickListener{changeText("L")}
        findViewById<Button>(R.id.btnKeyM).setOnClickListener{changeText("M")}
        findViewById<Button>(R.id.btnKeyN).setOnClickListener{changeText("N")}
        findViewById<Button>(R.id.btnKeyO).setOnClickListener{changeText("O")}
        findViewById<Button>(R.id.btnKeyP).setOnClickListener{changeText("P")}
        findViewById<Button>(R.id.btnKeyQ).setOnClickListener{changeText("Q")}
        findViewById<Button>(R.id.btnKeyR).setOnClickListener{changeText("R")}
        findViewById<Button>(R.id.btnKeyS).setOnClickListener{changeText("S")}
        findViewById<Button>(R.id.btnKeyT).setOnClickListener{changeText("T")}
        findViewById<Button>(R.id.btnKeyU).setOnClickListener{changeText("U")}
        findViewById<Button>(R.id.btnKeyV).setOnClickListener{changeText("V")}
        findViewById<Button>(R.id.btnKeyW).setOnClickListener{changeText("W")}
        findViewById<Button>(R.id.btnKeyX).setOnClickListener{changeText("X")}
        findViewById<Button>(R.id.btnKeyY).setOnClickListener{changeText("Y")}
        findViewById<Button>(R.id.btnKeyZ).setOnClickListener{changeText("Z")}
        findViewById<Button>(R.id.btnKeyDEL).setOnClickListener{changeText("")}
    }

    /*
        change text of active box
     */
    fun changeText(character:  String) {
        findViewById<EditText>(getResourceId("editText$activeBox")).text = Editable.Factory.getInstance().newEditable(character)
    }

    /*
        setup clock on the board
     */
    fun setupClock(action: String) {
        if(action == "start") {
            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);
        }
        else {
            timerHandler.removeCallbacks(timerRunnable);
        }
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
            "daily" -> return R.xml.crosswords
            "tourist1"-> return R.xml.tourist1
            "tourist2"-> return R.xml.tourist2
            "tourist3"-> return R.xml.tourist3
            "tourist4"-> return R.xml.tourist4
            "tourist5"-> return R.xml.tourist5
            "tourist6"-> return R.xml.tourist6
            "tourist7"-> return R.xml.tourist7
            else -> return 0
        }
    }

    /*
        calculate score of current session
     */
    private fun calScore(): Int {
        val threeLetter = 2
        val fourLetter = 4
        val fiveLetter = 4
        return (threeLetter)*5+(fourLetter*10)+(fiveLetter*15)
    }

    /*
        show popup upon game completion
    */
    fun showPopup() {
        try {

            finalTime = timerTextView.text.toString()
            finalScore = calScore()

            val myDialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            myDialog.setContentView(R.layout.gameover_popup)
            myDialog.show()

            myDialog.findViewById<TextView>(R.id.txtViewYourTime).text = Editable.Factory.getInstance().newEditable("Your Time : $finalTime")
            myDialog.findViewById<TextView>(R.id.txtViewYourScore).text = Editable.Factory.getInstance().newEditable("Your Score : $finalScore")

            myDialog.findViewById<Button>(R.id.btnGoHome).setOnClickListener {
                val intent = Intent(this, HomePageActivity::class.java)
                startActivity(intent)
            }

            myDialog.findViewById<Button>(R.id.buttonGOCross).setOnClickListener {
                myDialog.hide()
            }

            if (pack != "daily" && packLevel != 7) {
                val btnChangeBoard = myDialog.findViewById<Button>(R.id.btnChangeBoard)
                btnChangeBoard.visibility = View.VISIBLE
                btnChangeBoard.setOnClickListener{
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("pack", "tourist")
                    intent.putExtra("packLevel", packLevel+1)
                    startActivity(intent)
                }
            }
        }
        catch(ex: Exception) {
            Log.e("MainActivity", " error showing gameover popup ${ex.printStackTrace()}")
        }
    }
}
