package com.hhp227.application.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import com.hhp227.application.R
import com.hhp227.application.databinding.ActivityGroupBinding
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.fragment.TabHostLayoutFragment

class GroupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityGroupBinding.inflate(layoutInflater).root)
        (TabHostLayoutFragment.newInstance(intent.getParcelableExtra("group") ?: GroupItem.Group()) as TabHostLayoutFragment).also { fragMain ->
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

    fun onMyInfoActivityResult(result: ActivityResult) {
        for (fragment in supportFragmentManager.fragments) {
            if (fragment is TabHostLayoutFragment) {
                fragment.onMyInfoActivityResult(result)
            }
        }
    }
}
