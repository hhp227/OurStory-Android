package com.hhp227.application.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.hhp227.application.R
import com.hhp227.application.databinding.ActivityGroupBinding
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.fragment.GroupDetailFragment

class GroupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityGroupBinding.inflate(layoutInflater).root)
        GroupDetailFragment.newInstance(intent.getParcelableExtra("group") ?: GroupItem.Group()).also { fragMain ->
            supportFragmentManager.beginTransaction().replace(R.id.content_frame, fragMain).commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.group, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        R.id.actionChat -> {
            startActivity(Intent(this, ChatMessageActivity::class.java))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
