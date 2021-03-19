package com.coaker.newsaggregatorapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.coaker.newsaggregatorapp.ui.keywords.Keyword
import com.google.android.material.textfield.TextInputEditText
import java.lang.NullPointerException

/**
 * An activity class allowing users to add keywords to view news stories about.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
class KeywordSelectionActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var buttonSport: ImageButton
    private lateinit var buttonTech: ImageButton
    private lateinit var buttonPolitics: ImageButton
    private lateinit var buttonBusiness: ImageButton
    private lateinit var buttonEntertainment: ImageButton
    private lateinit var buttonMed: ImageButton
    private lateinit var buttonFood: ImageButton
    private lateinit var buttonGaming: ImageButton
    private lateinit var buttonMusic: ImageButton

    private var keywordsList = ArrayList<Keyword>()
    private var keywordButtons = ArrayList<ImageButton>()


    /**
     * A method called when the activity is being created. This sets up all the button variables
     * needed to configure them in the class.
     *
     * @param[savedInstanceState] Any previous saved instance of the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.keyword_selection_layout)

        buttonSport = findViewById(R.id.imageButtonSport)
        buttonTech = findViewById(R.id.imageButtonTechnology)
        buttonPolitics = findViewById(R.id.imageButtonPolitics)
        buttonBusiness = findViewById(R.id.imageButtonBusiness)
        buttonEntertainment = findViewById(R.id.imageButtonEntertainment)
        buttonMed = findViewById(R.id.imageButtonMedicine)
        buttonFood = findViewById(R.id.imageButtonFood)
        buttonGaming = findViewById(R.id.imageButtonGaming)
        buttonMusic = findViewById(R.id.imageButtonMusic)

        buttonSport.setOnClickListener(this)
        buttonTech.setOnClickListener(this)
        buttonPolitics.setOnClickListener(this)
        buttonBusiness.setOnClickListener(this)
        buttonEntertainment.setOnClickListener(this)
        buttonMed.setOnClickListener(this)
        buttonFood.setOnClickListener(this)
        buttonGaming.setOnClickListener(this)
        buttonMusic.setOnClickListener(this)

        keywordButtons.add(buttonSport)
        keywordButtons.add(buttonTech)
        keywordButtons.add(buttonPolitics)
        keywordButtons.add(buttonBusiness)
        keywordButtons.add(buttonEntertainment)
        keywordButtons.add(buttonMed)
        keywordButtons.add(buttonFood)
        keywordButtons.add(buttonGaming)
        keywordButtons.add(buttonMusic)

        val intent = intent

        try {
            keywordsList = intent.getParcelableArrayListExtra("keywordsList")!!
        } catch (e: NullPointerException) {

        }

        setupPreviousSelections()

        val buttonSubmit = findViewById<Button>(R.id.buttonSubmit)
        buttonSubmit.setOnClickListener(this)
    }


    /**
     * An onClick method used when any button in the activity layout is clicked.
     *
     * @param[v] The button that was clicked.
     */
    override fun onClick(v: View) {
        when (v.id) {
            R.id.imageButtonSport -> {
                selectButton(buttonSport)
            }

            R.id.imageButtonTechnology -> {
                selectButton(buttonTech)
            }

            R.id.imageButtonPolitics -> {
                selectButton(buttonPolitics)
            }

            R.id.imageButtonBusiness -> {
                selectButton(buttonBusiness)
            }

            R.id.imageButtonEntertainment -> {
                selectButton(buttonEntertainment)
            }

            R.id.imageButtonMedicine -> {
                selectButton(buttonMed)
            }

            R.id.imageButtonFood -> {
                selectButton(buttonFood)
            }

            R.id.imageButtonGaming -> {
                selectButton(buttonGaming)
            }

            R.id.imageButtonMusic -> {
                selectButton(buttonMusic)
            }

            R.id.buttonSubmit -> {
                populateKeywordsList()
                finish()
            }
        }
    }


    /**
     * A method that changes the appearance of a button to let the user know they have selected or
     * unselected that button.
     *
     * @param[button] The button that was selected or unselected.
     */
    private fun selectButton(button: ImageButton) {
        if (button.isSelected) {
            when (button) {
                buttonSport -> {
                    button.setImageResource(R.drawable.ic_baseline_sports_basketball_24)
                }

                buttonTech -> {
                    button.setImageResource(R.drawable.ic_baseline_laptop_24)
                }

                buttonPolitics -> {
                    button.setImageResource(R.drawable.ic_baseline_mic_24)
                }

                buttonBusiness -> {
                    button.setImageResource(R.drawable.ic_baseline_attach_money_24)
                }

                buttonEntertainment -> {
                    button.setImageResource(R.drawable.ic_baseline_format_paint_24)
                }

                buttonMed -> {
                    button.setImageResource(R.drawable.ic_baseline_medical_services_24)
                }

                buttonFood -> {
                    button.setImageResource(R.drawable.ic_baseline_fastfood_24)
                }

                buttonGaming -> {
                    button.setImageResource(R.drawable.ic_baseline_games_24)
                }

                buttonMusic -> {
                    button.setImageResource(R.drawable.ic_baseline_music_note_24)
                }
            }

            button.setBackgroundResource(R.drawable.circle_button)

        } else {
            button.setImageResource(R.drawable.ic_baseline_check_24)
            button.setBackgroundResource(R.drawable.circle_button_purple)
        }

        button.isSelected = !button.isSelected
    }


    /**
     * A method that populates the keyword list with custom keywords entered by the user, and preset
     * keywords which are selected by the user when they select a keyword button.
     */
    private fun populateKeywordsList() {
        val customText = findViewById<TextInputEditText>(R.id.editTextKeywords).text
        val customKeywords  = customText!!.split(",")

        val keywordStrings = ArrayList<String>()
        keywordsList.forEach { keyword ->
            keywordStrings.add(keyword.word.toString())
        }

        if (customKeywords.first() != "") {
            customKeywords.forEach {
                val keyword = Keyword()
                keyword.isNotifier = true
                keyword.word = it
                keywordsList.add(keyword)
            }
        }

        val newKeywordStrings = ArrayList<String>()

        keywordButtons.forEach {

            if (it.isSelected) {
                val keyword = Keyword()
                keyword.isNotifier = true
                when (it) {
                    buttonSport -> {
                        newKeywordStrings.add("Sport")
                    }

                    buttonTech -> {
                        newKeywordStrings.add("Technology")
                    }

                    buttonPolitics -> {
                        newKeywordStrings.add("Politics")
                    }

                    buttonBusiness -> {
                        newKeywordStrings.add("Business")
                    }

                    buttonEntertainment -> {
                        newKeywordStrings.add("Entertainment")
                    }

                    buttonMed -> {
                        newKeywordStrings.add("Medicine")
                    }

                    buttonFood -> {
                        newKeywordStrings.add("Food")
                    }

                    buttonGaming -> {
                        newKeywordStrings.add("Gaming")
                    }

                    buttonMusic -> {
                        newKeywordStrings.add("Music")
                    }
                }
            }
        }
        addKeywordToList(newKeywordStrings, keywordStrings)
    }


    /**
     * A method used when further keywords are being added to display to the user which keywords they
     * have selected already.
     */
    private fun setupPreviousSelections() {
        if (keywordsList.isNotEmpty()) {
            keywordsList.forEach {
                when (it.word) {
                    "Sport" -> {
                        buttonSport.isSelected = true
                        setButtonSelected(buttonSport)
                    }

                    "Technology" -> {
                        buttonTech.isSelected = true
                        setButtonSelected(buttonTech)
                    }

                    "Politics" -> {
                        buttonPolitics.isSelected = true
                        setButtonSelected(buttonPolitics)
                    }

                    "Business" -> {
                        buttonBusiness.isSelected = true
                        setButtonSelected(buttonBusiness)
                    }

                    "Entertainment" -> {
                        buttonEntertainment.isSelected = true
                        setButtonSelected(buttonEntertainment)
                    }

                    "Medicine" -> {
                        buttonMed.isSelected = true
                        setButtonSelected(buttonMed)
                    }

                    "Food" -> {
                        buttonFood.isSelected = true
                        setButtonSelected(buttonFood)
                    }

                    "Gaming" -> {
                        buttonGaming.isSelected = true
                        setButtonSelected(buttonGaming)
                    }

                    "Music" -> {
                        buttonMusic.isSelected = true
                        setButtonSelected(buttonMusic)
                    }
                }
            }
        }
    }


    /**
     * This method changes the appearance of the button being selected to let the user know this
     * button has been successfully selected.
     *
     * @param[button] The button being selected.
     */
    private fun setButtonSelected(button: ImageButton) {
        button.setImageResource(R.drawable.ic_baseline_check_24)
        button.setBackgroundResource(R.drawable.circle_button_purple)
    }


    /**
     * This method adds a new keyword to the user's keywords list.
     *
     * @param[newKeywordStrings] The new keyword to be added.
     * @param[keywordStrings] The list of keywords.
     */
    private fun addKeywordToList(newKeywordStrings: ArrayList<String>, keywordStrings: ArrayList<String>) {
        newKeywordStrings.forEach {
            if (!keywordStrings.contains(it)) {
                val newKeyword = Keyword()
                newKeyword.word = it
                newKeyword.isNotifier = true
                keywordsList.add(newKeyword)
            }
        }

    }


    /**
     * This method is called when the activity is being closed and sends the keywords list back to
     * the main activity.
     */
    override fun finish() {
        val data = Intent()
        data.putParcelableArrayListExtra("keywordsList", keywordsList)
        setResult(Activity.RESULT_OK, data)

        super.finish()
    }
}





