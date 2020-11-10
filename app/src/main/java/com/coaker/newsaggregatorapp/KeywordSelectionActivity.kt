package com.coaker.newsaggregatorapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

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

    private var keywordsList = ArrayList<String>()
    private var keywordButtons = ArrayList<ImageButton>()

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

        val buttonSubmit = findViewById<Button>(R.id.buttonSubmit)
        buttonSubmit.setOnClickListener(this)
    }

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

    private fun populateKeywordsList() {
        val customText = findViewById<TextInputEditText>(R.id.editTextKeywords).text
        val customKeywords  = customText!!.split(",")

        if (customKeywords.first() != "") {
            customKeywords.forEach {
                keywordsList.add(it)
            }
        }

        keywordButtons.forEach {
            if (it.isSelected) {
                when (it) {
                    buttonSport -> {
                        keywordsList.add("Sport")
                    }

                    buttonTech -> {
                        keywordsList.add("Technology")
                    }

                    buttonPolitics -> {
                        keywordsList.add("Politics")
                    }

                    buttonBusiness -> {
                        keywordsList.add("Business")
                    }

                    buttonEntertainment -> {
                        keywordsList.add("Entertainment")
                    }

                    buttonMed -> {
                        keywordsList.add("Medicine")
                    }

                    buttonFood -> {
                        keywordsList.add("Food")
                    }

                    buttonGaming -> {
                        keywordsList.add("Gaming")
                    }

                    buttonMusic -> {
                        keywordsList.add("Music")
                    }
                }
            }
        }
    }

    override fun finish() {
        val data = Intent()
        data.putStringArrayListExtra("keywordsList", keywordsList)
        setResult(Activity.RESULT_OK, data)

        super.finish()
    }
}




