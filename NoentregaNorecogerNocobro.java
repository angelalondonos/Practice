public class FormNoCompraActivity extends CustomActivity implements Sincronizador {

	int anchoImg, altoImg;

	Vector<MotivoCompra> listaMotivosNoCompra;
	Vector<MotivoCompra> listaMotivosNoCobro;
	Vector<SubMotivoCompra> listaSubMotivosNoCompra;

	boolean esModificado;
	boolean esEntrega;

	EditText etComentarios;
	CobroProgramado cp;

	private Handler hTiempoEspera;
	private long tEspera = 45 * 1000;

	ProgressDialog progressDialog;

	AlertDialog alertDialog;

	Drawable fotoActual;
	private Activity context = null;
	private String mensaje = "";
	String numeroRegistro;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.form_no_compra);

		Inicializar();
		InicializarFotos();

		if (esEntrega) {

			CargarMotivosNoCompra();
			CargarSubMotivosNoCompra();
			setTitle("No Entrega");
		} else {

			CargarMotivosNoCobro();
			setTitle("No Cobro");
		}

		((TableLayout) findViewById(R.id.tblLayoutNoCompraSub)).setVisibility(View.GONE);

	}

	public void Inicializar() {

		esModificado = false;
		esEntrega = false;
		cp = null;

		Bundle bundle = getIntent().getExtras();

		if (bundle != null && bundle.containsKey("modificar"))
			esModificado = bundle.getBoolean("modificar");

		if (bundle != null && bundle.containsKey("esEntrega"))
			esEntrega = bundle.getBoolean("esEntrega");

		if (bundle != null && bundle.containsKey("cp"))
			cp = (CobroProgramado) bundle.get("cp");

		//INICIALIZAR COMPONENTES
		etComentarios = findViewById(R.id.etComentarios);

	}

	public void setPhotoDefault() {

		Drawable fotoVacia = getResources().getDrawable(R.drawable.foto_vacia);
		Drawable img = Util.ResizedImage(fotoVacia, anchoImg, altoImg);

		if (img != null)
			((ImageView) findViewById(R.id.imageFoto)).setImageDrawable(img);
	}

	private void InicializarFotos() {

		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int ancho = display.getWidth();
		int alto = display.getHeight();

		anchoImg = ancho;//(ancho * 100) / 480;
		altoImg = alto;//(alto * 130) / 640;

		if (fotoActual != null) {

			((ImageView) findViewById(R.id.imageFoto)).setImageDrawable(fotoActual);

		} else {

			setPhotoDefault();
		}
	}

	public void OnClickFormNoCompra(View view) {

		switch (view.getId()) {

			case R.id.btnAceptarFormNoCompra:
				GuardarMotivoNoCompra();
				break;

			case R.id.btnCancelarFormNoCompra:
				Finalizar(false);
				break;
		}
	}

	public void Finalizar(boolean ok) {

		Main.fotoNoCompra = null;
		//BorrarFotoCapturada();
		if (ok)
			setResult(RESULT_OK);
		finish();
	}

	public void OnClickTomarFoto(View view) {

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		String filePath = Util.DirApp().getPath() + "/foto.jpg";

		Uri output = FileProvider.getUriForFile(FormNoCompraActivity.this,
				"celuweb.com.nutresaEntregas", new File(filePath));

		intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
		startActivityForResult(intent, Const.RESP_TOMAR_FOTO);
	}


	public void OnClickEliminarFoto(View view) {

		if (fotoActual != null) {

			fotoActual = null;
			setPhotoDefault();
		}
	}

	public void CargarMotivosNoCompra() {
		ArrayAdapter<String> adapter;
		Vector<String> listaItems = new Vector<String>();
		listaMotivosNoCompra = DataBaseBO.ListaMotivosNoCompra(listaItems);

		if (listaItems.size() > 0) {
			String[] items = new String[listaItems.size()];
			listaItems.copyInto(items);
			adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, items);
		} else {
			adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, new String[]{});
		}

		Spinner spinner = (Spinner) findViewById(R.id.cbNoCompra);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

				MotivoCompra mc = listaMotivosNoCompra.elementAt(position);

				if (mc.tSubMenu.equals("1")) {

					((TableLayout) findViewById(R.id.tblLayoutNoCompraSub)).setVisibility(View.VISIBLE);
				} else {

					((TableLayout) findViewById(R.id.tblLayoutNoCompraSub)).setVisibility(View.GONE);
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
			}
		});
	}

	public void CargarSubMotivosNoCompra() {

		ArrayAdapter<String> adapter;
		Vector<String> listaItems = new Vector<String>();
		listaSubMotivosNoCompra = DataBaseBO.ListaSubMotivosNoCompra(listaItems, "");

		if (listaItems.size() > 0) {

			String[] items = new String[listaItems.size()];
			listaItems.copyInto(items);
			adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, items);

		} else {		
    		adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, new String[]{});
		}
		Spinner spinner = (Spinner) findViewById(R.id.cbNoCompraSub);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

	}

	public void CargarMotivosNoCobro() {

		ArrayAdapter<String> adapter;
		Vector<String> listaItems = new Vector<String>();
		listaMotivosNoCobro = DataBaseBO.ListaMotivosNoCobro(listaItems);

		if (listaItems.size() > 0) {
			String[] items = new String[listaItems.size()];
			listaItems.copyInto(items);
			adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, items);
		} else {
			adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, new String[]{});
		}

		Spinner spinner = (Spinner) findViewById(R.id.cbNoCompra);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
	}

	public boolean GuardarMotivoNoCompra() {
		String motivo;
		String subMotivo;
		String numRegistro;
		String redespacho = "";
		String comentarios = "";

		if (true) {

			comentarios = etComentarios.getText().toString();
			String version = ObtenerVersion();
			Spinner spinner = (Spinner) findViewById(R.id.cbNoCompra);
			int position = spinner.getSelectedItemPosition();
			int numMotivos = 0;
			boolean bien = false;

			if (esEntrega) {
    			numMotivos = listaMotivosNoCompra.size();
			} else {
				numMotivos = listaMotivosNoCobro.size();
			}

			if (position != AdapterView.INVALID_POSITION && numMotivos > 0) {
				motivo = "";
				subMotivo = "";
				numRegistro = "";

				if (esEntrega) {
					motivo = listaMotivosNoCompra.elementAt(position).codigo;
					if (motivo.equals("")) {
						MostrarAlertDialog("Debe elegir un motivo");
						return false;
					}
					if (listaMotivosNoCompra.elementAt(position).tSubMenu.equals("1")) {
						spinner = (Spinner) findViewById(R.id.cbNoCompraSub);
						position = spinner.getSelectedItemPosition();
						subMotivo = listaSubMotivosNoCompra.elementAt(position).codigo;
					}
					byte[] byteArray = null;
    				if (fotoActual != null) {
						Bitmap bitmap = ((BitmapDrawable) fotoActual).getBitmap();
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
						byteArray = stream.toByteArray();
					}

					CheckBox checkBox = (CheckBox) findViewById(R.id.cbRedespacho);
					if (checkBox.isChecked()) {
						redespacho = "REDESPACHO";
						subMotivo = redespacho;
					}
					bien = DataBaseBO.terminarNoEntrega(Main.factEntregar, motivo, version, subMotivo, byteArray,comentarios);

					if (bien) {
						numRegistro = "1234567899";
						if (Main.factEntregar.tipoTrans.equals("0") && !checkBox.isChecked()) {
							Vector<DetalleEnt> listDetalle = DataBaseBO.getDetalleEntrega(Main.factEntregar.numeroDoc);
							if (!DataBaseBO.guardarDevolucionNoEntrega(Main.factEntregar, listDetalle)) {
								MostrarAlertDialogEnviar("Problema almacenando la informacion de devolucion, por favor intente nuevamente", "");
							}
						}

					} else {

						numRegistro = "";
						MostrarAlertDialogEnviar("Problema almacenando la informacion, por favor intente nuevamente", "");
					}
				} else {

					motivo = listaMotivosNoCobro.elementAt(position).codigo;

					if (motivo.equals("")) {
						MostrarAlertDialog("Debe elegir un motivo");
						return false;
					}
					byte[] byteArray = null;

					if (fotoActual != null) {

						Bitmap bitmap = ((BitmapDrawable) fotoActual).getBitmap();
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
						byteArray = stream.toByteArray();
					}

					CheckBox checkBox = (CheckBox) findViewById(R.id.cbRedespacho);

					if (checkBox.isChecked()) {
						redespacho = "REDESPACHO";
					}


					numRegistro = DataBaseBO.guardarCobro(cp, motivo, "0", "CP", Main.usuario.codigoVendedor, byteArray, redespacho, comentarios);

				}

				if (!numRegistro.equals("")) {

					Main.fotoNoCompra = null;
					//BorrarFotoCapturada();
					setResult(RESULT_OK);
					finish();
				}
				return true;
			} else {
				MostrarAlertDialog("No se pudo Registrar la No Entrega!");
				return false;
			}
		} else {
			int subPosition = 0;
			if (subPosition != AdapterView.INVALID_POSITION && listaSubMotivosNoCompra.size() == 0) {
				MostrarAlertDialog("Para Registar la No Entrega, Debe primero seleccionar un submotivo");
				return false;

			} else {

				Spinner spinner = (Spinner) findViewById(R.id.cbNoCompra);
				int position = spinner.getSelectedItemPosition();

				if (position != AdapterView.INVALID_POSITION && listaMotivosNoCompra.size() > 0) {

					if (esModificado) {
					}

					MotivoCompra motivoCompra = listaMotivosNoCompra.elementAt(position);

					if (motivoCompra.codigo.equals("")) {
						MostrarAlertDialog("Debe elegir un motivo");
						return false;
					}


					SubMotivoCompra subMotiv = listaSubMotivosNoCompra.elementAt(subPosition);

					//if (DataBaseBO.GuardarNoCompra(Main.encabezado, version, imei, byteArray)) {
					if (DataBaseBO.GuardarEntregaRealizada(Main.detalleFacturas, false, true, ("" + motivoCompra.codigo), subMotiv.codigo)) {


						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setMessage("Novedad Registrada con Exito")
								.setCancelable(false)
								.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog, int id) {

										if (esModificado) {

											DataBaseBO.asignarEntregasSinEnviar();
										}

										dialog.cancel();

										Finalizar(true);

										}
								});


						AlertDialog alert = builder.create();
						alert.show();

						return true;

					} else {

						MostrarAlertDialog("No se pudo Registrar la No Entrega!");
						return false;
					}

				} else {

					MostrarAlertDialog("No se pudo Procesar la Imagen. Por Favor intente nuevamente!");
					return false;
				}
			}
		}
	}

	public void imprimirTirillaDevolucion(final String macAddress, final String numRegistro) {

		FormNoCompraActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				progressDialog = ProgressDialog.show(FormNoCompraActivity.this, "",
						"Por Favor Espere...\n\nProcesando Informacion!", false);
				progressDialog.show();
			}
		});

		new Thread(new Runnable() {

			public void run() {

				try {

					String contenido = PrinterBO.tirillaNoCobro(numRegistro);

					Impresora impresora = new Impresora();
					impresora.setMacAddress(macAddress);
					impresora.setContenido(contenido);

					boolean bien = impresora.imprimirTirilla();

					Thread.sleep(500);

					if (bien) {

						handlerFinish.sendEmptyMessage(0);

					} else {

						mensaje = impresora.getMensaje();
						context = FormNoCompraActivity.this;
						handlerMensaje.sendEmptyMessage(0);
					}
				} catch (Exception e) {
					mensaje = "No se pudo imprimir: " + e.getMessage();
					context = FormNoCompraActivity.this;
					handlerMensaje.sendEmptyMessage(0);
				} finally {
					FormNoCompraActivity.this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							MostrarAlertDialogEnviar("Novedad Registrada con Exito", numRegistro);
						}
					});
				}
			}

		}).start();
	}

	private Handler handlerFinish = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			if (progressDialog != null) {

				progressDialog.cancel();
			}
		}
	};

	private Handler handlerMensaje = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			if (context != null) {

				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setCancelable(false).setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {

						dialog.cancel();
					}
				});

				AlertDialog alertDialog = builder.create();
				alertDialog.setMessage(mensaje);
				alertDialog.show();
			}

			if (progressDialog != null) {

				progressDialog.cancel();
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == Const.RESP_TOMAR_FOTO && resultCode == RESULT_OK) {

			System.gc();

			fotoActual = ResizedImage(anchoImg, altoImg);

			if (fotoActual != null) {
				ImageView imgFoto = (ImageView) findViewById(R.id.imageFoto);
				imgFoto.setImageDrawable(fotoActual);
		
			}

			BorrarFotoCapturada();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			Finalizar(false);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}


	public Drawable ResizedImage(int newWidth, int newHeight) {

		Matrix matrix;
		Bitmap resizedBitmap = null;
		Bitmap bitmapOriginal = null;

		try {

			File fileImg = new File(Util.DirApp(), "foto.jpg");

			if (fileImg.exists()) {

				bitmapOriginal = decodeFile(fileImg, 320, 480);

				int width = bitmapOriginal.getWidth();
				int height = bitmapOriginal.getHeight();

				if (width == newWidth && height == newHeight) {

					return new BitmapDrawable(bitmapOriginal);
				}

				// Reescala el Ancho y el Alto de la Imagen
				float scaleWidth = ((float) newWidth) / width;
				float scaleHeight = ((float) newHeight) / height;

				matrix = new Matrix();
				matrix.postScale(scaleWidth, scaleHeight);

				// Crea la Imagen con el nuevo Tamano
				resizedBitmap = Bitmap.createBitmap(bitmapOriginal, 0, 0, width, height, matrix, true);

				return new BitmapDrawable(resizedBitmap);
			}

			return null;

		} catch (Exception e) {

			Toast.makeText(this, e.toString(), Toast.LENGTH_LONG);
			return null;

		} finally {

			matrix = null;

			if (resizedBitmap != null) {

				if (resizedBitmap.isRecycled())
					resizedBitmap.recycle();
			}

			if (bitmapOriginal != null) {
				if (bitmapOriginal.isRecycled())
					bitmapOriginal.recycle();
			}

			resizedBitmap = null;
			bitmapOriginal = null;
			System.gc();
		}
	}

	public static Bitmap decodeFile(File f, int WIDTH, int HIGHT) {
		try {
			//Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			//The new size we want to scale to
			final int REQUIRED_WIDTH = WIDTH;
			final int REQUIRED_HIGHT = HIGHT;
			//Find the correct scale value. It should be the power of 2.
			int scale = 1;
			while (o.outWidth / scale / 2 >= REQUIRED_WIDTH && o.outHeight / scale / 2 >= REQUIRED_HIGHT)
				scale *= 2;

			//Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	public int ObtenerOrientacionImg() {

		String[] projection = {MediaStore.Images.ImageColumns.ORIENTATION};

		int orientacion = 0;
		Cursor cursor = null;
		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

		try {

			if (uri != null) {

				cursor = managedQuery(uri, projection, null, null, null);
			}

			if (cursor != null && cursor.moveToLast()) {

				orientacion = cursor.getInt(0);
				Log.i("ObtenerOrientacionImg", "ORIENTATION = " + orientacion);
			}

		} finally {
			stopManagingCursor(cursor);
		}

		return orientacion;
	}

	public void BorrarFotoCapturada() {

		String[] projection = {MediaStore.Images.ImageColumns.SIZE,
				MediaStore.Images.ImageColumns.DISPLAY_NAME,
				MediaStore.Images.ImageColumns.DATA,
				BaseColumns._ID,
		};

		Cursor cursor = null;
		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

		try {

			if (uri != null) {

				cursor = managedQuery(uri, projection, null, null, null);
			}

			if (cursor != null && cursor.moveToLast()) {

				ContentResolver contentResolver = getContentResolver();
				int rows = contentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, BaseColumns._ID + "=" + cursor.getString(3), null);

				Log.i("BorrarFotoCapturada", "Numero de filas eliminadas : " + rows);
			}

		} finally {
			stopManagingCursor(cursor);
		}
	}

	public String ObtenerVersion() {

		String version;

		try {

			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;

		} catch (NameNotFoundException e) {

			version = "0.0";
			Log.e("FormNoCompraActivity", e.getMessage(), e);
		}

		return version;
	}

	public String ObtenerImei() {

		TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return null;
		}
		return manager.getDeviceId();
	}

	public void MostrarAlertDialog(String mensaje) {

		AlertDialog alertDialog;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false).setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {

				dialog.cancel();				
			}
		});

		alertDialog = builder.create();
		alertDialog.setMessage(mensaje);
		alertDialog.show();
	}

	public void MostrarAlertDialogEnviar(String mensaje,final String numeroRegistro ) {
		

		MostrarAlertDialogCerrar( mensaje );
	}

	public void MostrarAlertDialogCerrar(String mensaje) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false).setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {

				dialog.cancel();
				
				Finalizar( true );
			}
		});

		alertDialog = builder.create();

		alertDialog.setMessage(mensaje);
		alertDialog.show();
	}

	@Override
	public void RespSync(boolean ok, String respuestaServer, String msg, int codeRequest) {

		if( hTiempoEspera != null )
			hTiempoEspera.removeCallbacks( mTerminarSincronizacion );

		switch (codeRequest) {

		case Const.ENVIAR_PEDIDO:
			RespuestaEnviarInfo(ok, respuestaServer, msg);
			break;

		default:
			if (progressDialog != null)
				progressDialog.cancel();
			break;
		}
	}

	public void RespuestaEnviarInfo(boolean ok, String respuestaServer, String msg) {

		final String mensaje = ok ? "Informacion Registrada con Exito en el servidor" : msg;		

		if (progressDialog != null)
			progressDialog.cancel();

		this.runOnUiThread(new Runnable() {

			public void run() {

				MostrarAlertDialogCerrar( mensaje );
			}
		});
	}

	private Runnable mTerminarSincronizacion = new Runnable()
	{ 
		public void run() {

			hTiempoEspera.removeCallbacks( mTerminarSincronizacion ); 

			if( progressDialog != null )
				progressDialog.dismiss();

			MostrarAlertDialogCerrar( "El tiempo de espera ha terminado" );
		} 
	};
}
