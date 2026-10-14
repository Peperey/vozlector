package com.example.vozlector

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private lateinit var salida: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PDFBoxResourceLoader.init(applicationContext)

        val boton = Button(this).apply {
            text = "Elegir PDF"
            setOnClickListener {
                val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/pdf"
                }
                startActivityForResult(i, 1)
            }
        }
        salida = TextView(this).apply { setPadding(24, 24, 24, 24) }

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(boton)
            addView(ScrollView(this@MainActivity).apply { addView(salida) })
        })
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri: Uri = data?.data ?: return
        if (requestCode != 1 || resultCode != RESULT_OK) return
        salida.text = "Leyendo PDF..."
        thread {
            val texto = try {
                contentResolver.openInputStream(uri)!!.use { input ->
                    PDDocument.load(input).use { doc ->
                        PDFTextStripper().getText(doc)
                    }
                }
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
            val vista = if (texto.isBlank()) "(Sin texto: ¿PDF escaneado?)" else texto.take(5000)
            runOnUiThread { salida.text = vista }
        }
    }
}
