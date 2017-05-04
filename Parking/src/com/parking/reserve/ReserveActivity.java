package com.parking.reserve;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;

import com.parking.R;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ReserveActivity extends Activity {

	CustomDialog.Builder builder;
	String parkingName;
	String total;
	String remain;// ������
	String price;
	TextView name;
	TextView totalView;
	TextView remainView;
	TextView priceView;
	EditText hour;
	EditText minute;
	Button reserve;
	Button cancle;
	Button stop;
	TextView text;
	int endHour;
	int endMin;
	Long recLen;
	String objectId;
	int number;// ����
	int count = 1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reserve);
		/*
		 * name = (TextView) findViewById(R.id.parkingName); totalView =
		 * (TextView) findViewById(R.id.total); remainView = (TextView)
		 * findViewById(R.id.remain); priceView = (TextView)
		 * findViewById(R.id.price);
		 */
		text = (TextView) findViewById(R.id.text);
		hour = (EditText) findViewById(R.id.hour);
		minute = (EditText) findViewById(R.id.minute);
		reserve = (Button) findViewById(R.id.reserve);
		cancle = (Button) findViewById(R.id.cancle);
		cancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO �Զ����ɵķ������
				deleteTable();
               
			}
		});
		stop=(Button) findViewById(R.id.stop);
		stop.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO �Զ����ɵķ������
				text.setText("�����κ�ԤԼ");
				stop.setVisibility(View.INVISIBLE);
				cancle.setVisibility(View.INVISIBLE);
				count=0;
			}
			
		});
		builder = new CustomDialog.Builder(this);
		builder.setMessage("����ԤԼʱ���ѵ�δ���� ԤԼ��ȡ�� ���ٴ�Ԥ��");
		builder.setTitle("��ʾ");
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// ������Ĳ�������
			}
		});

		builder.setNegativeButton("ȡ��",
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		reserve.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO �Զ����ɵķ������
				if(addReserveInfo()==false){
					Toast.makeText(ReserveActivity.this, "�Ѵ���ԤԼ�������ٴ�ԤԼ", Toast.LENGTH_LONG).show();;
				}
			}

		});
	}

	private boolean addReserveInfo() {
		// TODO �Զ����ɵķ������
		if(objectId!=null){
			return false;
		}
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		Date curDate = new Date(System.currentTimeMillis());
		String startTime = format.format(curDate);
		String str[] = startTime.split(":");
		String startHour = str[0];
		String startMin = str[1];
		int startHour2 = Integer.parseInt(startHour);
		int startMin2 = Integer.parseInt(startMin);

		endHour = Integer.parseInt(hour.getText().toString());
		endMin = Integer.parseInt(minute.getText().toString());
		if (!text.getText().equals("�����κ�ԤԼ")) {
			Toast.makeText(ReserveActivity.this, "��ǰ����ԤԼ", Toast.LENGTH_LONG)
					.show();
		} else if (endHour < startHour2) {
			Toast.makeText(ReserveActivity.this, "ԤԼʱ�䲻�����ڵ�ǰʱ��",
					Toast.LENGTH_LONG).show();
		} else if (endHour == startHour2 && startMin2 > endMin) {
			Toast.makeText(ReserveActivity.this, "ԤԼʱ�䲻�����ڵ�ǰʱ��",
					Toast.LENGTH_LONG).show();
		} else if ((endHour - startHour2) > 2) {
			Toast.makeText(ReserveActivity.this, "ԤԼʱ�䲻�ɳ�������Сʱ",
					Toast.LENGTH_LONG).show();
		} else {
			// ���и���
			addToTable();
			Toast.makeText(ReserveActivity.this, "ԤԼ�ɹ�", Toast.LENGTH_LONG)
					.show();
			cancle.setVisibility(View.VISIBLE);
			Long end = (long) (endHour * (3600 * 1000) + endMin * (60 * 1000));
			Long start = (long) (startHour2 * (3600 * 1000) + startMin2
					* (60 * 1000));
			recLen = (end - start) / 1000;
			new Thread(new MyThread()).start();

			// text.setTimes(end);

		}
        return true;
	}

	private void addToTable() {
		// TODO �Զ����ɵķ������

		BmobQuery<ParkinglotInfo> query = new BmobQuery<ParkinglotInfo>();
		query.addWhereEqualTo("parkinglot_name", parkingName);
		query.findObjects(new FindListener<ParkinglotInfo>() {

			@Override
			public void done(List<ParkinglotInfo> arg0, BmobException arg1) {
				// TODO �Զ����ɵķ������
				if (arg1 == null) {
					Toast.makeText(ReserveActivity.this,
							"��ѯ�ɹ�����" + arg0.size() + "�����ݡ�", Toast.LENGTH_LONG)
							.show();
					for (ParkinglotInfo pk : arg0) {

						objectId = pk.getObjectId();
						number = pk.getCurrentLeftNum();
					}
				} else {
					Log.i("bmob",
							"ʧ�ܣ�" + arg1.getMessage() + ","
									+ arg1.getErrorCode());
				}
			}

		});
		if (number != 0) {
			ParkinglotInfo pk = new ParkinglotInfo();
			pk.setCurrentLeftNum(number - 1);// ����-1
			pk.update(objectId, new UpdateListener() {

				@Override
				public void done(BmobException arg0) {
					// TODO �Զ����ɵķ������
					if (arg0 == null) {
						Toast.makeText(ReserveActivity.this, "���³ɹ�",
								Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(ReserveActivity.this, "����ʧ��",
								Toast.LENGTH_LONG).show();
					}
				}

			});

		}
		SharedPreferences pkInfo = getSharedPreferences("pkInfo", MODE_PRIVATE);
		SharedPreferences.Editor editor = pkInfo.edit();
		editor.putString("pkId", objectId);
		editor.commit();

	}

	final Handler handler = new Handler() { // handle
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				recLen--;
				if (recLen == 0) {
					text.setText("�����κ�ԤԼ");
					deleteTable();
					builder.create().show();
					cancle.setVisibility(View.GONE);
					stop.setVisibility(View.GONE);
					break;
				}
				text.setText(recLen + "s");
			}
			super.handleMessage(msg);
		}

	};

	// ����ԤԼʱ�� ԤԼȡ��
	private void deleteTable() {
		// TODO �Զ����ɵķ������
		SharedPreferences pkInfo = getSharedPreferences("pkInfo", MODE_PRIVATE);
		SharedPreferences.Editor editor = pkInfo.edit();
		objectId=pkInfo.getString("pkId", "");
		BmobQuery<ParkinglotInfo> bmobQuery = new BmobQuery<ParkinglotInfo>();
		bmobQuery.getObject("objectId", new QueryListener<ParkinglotInfo>() {
			public void done(ParkinglotInfo object, BmobException e) {
				if (e == null) {
					number = object.getCurrentLeftNum();
				} else {

				}
			}
		});
		ParkinglotInfo pk = new ParkinglotInfo();
		pk.setCurrentLeftNum(number + 1);// ���м�һ
		pk.update(objectId, new UpdateListener() {

			@Override
			public void done(BmobException arg0) {
				// TODO �Զ����ɵķ������
				if (arg0 == null) {
					Toast.makeText(ReserveActivity.this, "���³ɹ�",
							Toast.LENGTH_LONG).show();
					cancle.setVisibility(View.GONE);
					count = 0;
				} else {
					Toast.makeText(ReserveActivity.this, "����ʧ��",
							Toast.LENGTH_LONG).show();
				}
			}

		});
		editor.putString("pkId", "");
		editor.commit();
	}

	class MyThread implements Runnable { 
		
		public void run() {
			count=1;
			while (true) {
				try {
					if (count != 0) {
						Thread.sleep(1000); // sleep 1000ms
						Message message = new Message();
						message.what = 1;
						handler.sendMessage(message);
					} else {
						break;
					}
				} catch (Exception e) {

				}
			}

		}
	}
}
