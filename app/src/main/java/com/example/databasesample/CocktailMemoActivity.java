package com.example.databasesample;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class CocktailMemoActivity extends AppCompatActivity {
    /* 選択されたカクテルの主キーID */
    int _cocktailId = -1;
    /* 選択されたカクテル名 */
    String _cocktailName = "";
    /* カクテル名を表示するTextView */
    TextView _tvCocktailName;
    Button _btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cocktail_memo);

        _tvCocktailName = findViewById(R.id.tvCocktailName);
        _btnSave = findViewById(R.id.btnSave);
        ListView lvCocktail = findViewById(R.id.lvCocktail);
        lvCocktail.setOnItemClickListener(new ListItemClickListener());
    }

    private class ListItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            _cocktailId = position;
            _cocktailName = (String) parent.getItemAtPosition(position);
            //　カクテル名を表示するTextView
            _tvCocktailName.setText(_cocktailName);
            _btnSave.setEnabled(true);

            // データベースヘルパーオブジェクト生成
            DatabaseHelper helper = new DatabaseHelper(CocktailMemoActivity.this);
            // データベース接続オブジェクト取得
            SQLiteDatabase db = helper.getWritableDatabase();

            try {
                // 主キーによる検索SQL文字列の用意
                String sql = "SELECT * FROM cocktailmemo WHERE _id = " + _cocktailId;
                // SQLの実行
                Cursor cursor = db.rawQuery(sql, null);
                // データベースから取得した値を格納する変数の用意。データがなかったときの初期値も用意。
                String note = "";
                // SQLの戻り値であるカーソルオブジェクトをループさせてデータベース内のデータを取得
                while (cursor.moveToNext()) {
                    // カラムのインデックス値を取得
                    int idxNote = cursor.getColumnIndex("note");
                    // カラムのインデックス値をもとに実際のデータを取得
                    note = cursor.getString(idxNote);
                }
                EditText etNote = findViewById(R.id.etNote);
                etNote.setText(note);

            } finally {
                // データベース接続オブジェクトの開放
                db.close();
            }
        }
    }

    public void onSaveButtonClick(View view) {
        // 入力された感想を取得
        EditText etNote = findViewById(R.id.etNote);
        String note = etNote.getText().toString();

        // データベースヘルパーオブジェクト生成
        DatabaseHelper helper = new DatabaseHelper(CocktailMemoActivity.this);
        // データベースヘルパーオブジェクトからデータベース接続オブジェクトを取得
        SQLiteDatabase db = helper.getWritableDatabase();

        try {
            // まず、リストで選択されたカクテルのメモデータを削除。その後インサートを行う。
            // 削除用SQL文字列を用意。
            String sqlDelete = "DELETE FROM cocktailmemo WHERE _id = ?";
            // SQL文字列をもとにプリペアードステートメントを取得
            SQLiteStatement stmt = db.compileStatement(sqlDelete);
            // 変数のバインド
            stmt.bindLong(1, _cocktailId);
            // 削除SQLの実行
            stmt.executeUpdateDelete();

            // インサート用SQL文字列を用意
            String sqlInsert = "INSERT INTO cocktailmemo (_id, name, note) VALUES (?, ?, ?)";
            // SQL文字列をもとにプリペアードステートメントを取得
            stmt = db.compileStatement(sqlInsert);
            // 変数のバインド
            stmt.bindLong(1, _cocktailId);
            stmt.bindString(2, _cocktailName);
            stmt.bindString(3, note);
            // インサート用SQLの実行
            stmt.executeInsert();

        } finally {
            // データベース接続オブジェクトの開放
            db.close();
        }

        // カクテル名を「未選択」に変更
        _tvCocktailName.setText(getString(R.string.tv_name));
        etNote.setText("");
        _btnSave.setEnabled(false);
    }
}