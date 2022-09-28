
public class FormFactPendienteActivity extends CustomActivity /*implements GPS*/ {

	int ciudadSeleccionada;
	boolean primerEjecucion = true;
	Vector<EncabezadoEnt> listaFactEntregar;
	String codCliente = "";
	Context context;
	TextView tvValorEntrega, tvValorRecogida, txValorCobro;
	int reversa = 0;

	GPSListener gpsListener;
	LocationManager locationManager;
	Location currentLocation = null;

	public void mostrarEjecucion() {

		boolean result = DataBaseBO.consultarEjecucion(codCliente);

		if (result) {
			((Button) findViewById(R.id.btnEjecucion)).setVisibility(View.VISIBLE);
		} else {
			((Button) findViewById(R.id.btnEjecucion)).setVisibility(View.GONE);
		}
	}

	public void onClickInicioEspera(View view) {
		marcarEspera();
	}
	
	public void CargarFacturasPendientes() {
		
		ItemListView[] items;
		tvValorEntrega = (TextView) findViewById(R.id.tvValorEntrega);
		tvValorRecogida = (TextView) findViewById(R.id.tvValorRecogida);
		txValorCobro = (TextView) findViewById(R.id.txValorCobro);
		
		Cliente c= DataBaseBO.CargarCliente(codCliente);
		Main.cliente = c;
		
		((TextView) findViewById(R.id.tvNombreCliente)).setText(codCliente+"-"+c.nombre);
		((TextView) findViewById(R.id.tvRazonSocial)).setText(c.razonSocial);
		((TextView) findViewById(R.id.txDireccion)).setText(c.direccion);
		
		Vector<ItemListView> listaItems = new Vector<ItemListView>();
		listaFactEntregar = DataBaseBO.ListaFacturasEntregar(listaItems, codCliente, context, tvValorEntrega, tvValorRecogida, txValorCobro);
		
		if (listaItems.size() > 0) {

			items = new ItemListView[listaItems.size()];
			listaItems.copyInto(items);

		} else {

			items = new ItemListView[] {};

			if (listaFactEntregar != null)
				listaFactEntregar.removeAllElements();
		}

		ListViewAdapter adapter = new ListViewAdapter(this, items, R.drawable.symbol_normal, R.color.verde_secund);
		ListView listaFacturasEntregar = (ListView) findViewById(R.id.listaFactEntregar);
		listaFacturasEntregar.setAdapter(adapter);
		
		(( TextView )findViewById( R.id.lalNumFactEntregar )).setText("No. Documentos: " + String.valueOf( listaFactEntregar.size() ) );
	}
	
	public void onClickInicioCliente(View view){		
		marcarSalida();
	}
	
	public void onClickFinCliente(View view){		
		marcarSalida();	
	}

	private void marcarSalida() {
		String cargue = DataBaseBO.getNumeroCargues( Main.usuario.codigoVendedor );
		
		InicioFinCliente inicioFinCliente = new InicioFinCliente();
		inicioFinCliente.cargue = cargue;
		inicioFinCliente.codCliente = codCliente;
		inicioFinCliente.entregador = Main.usuario.codigoVendedor;
		inicioFinCliente.fechaFinal = "";
		inicioFinCliente.fechaInicial = Util.FechaActual("yyyy-MM-dd HH:mm:ss");
		inicioFinCliente.id = "E" + Util.ObtenerFechaId();
		
		DataBaseBO.ValidarUsuario();

		String idTemp = "" + DataBaseBO.validarInicioFinTemp(inicioFinCliente.cargue, inicioFinCliente.codCliente);
		
		if (idTemp.equals("")) {
			DataBaseBO.copiaRegistroLlegada(inicioFinCliente.cargue, inicioFinCliente.codCliente);
		}
		String id = "" + DataBaseBO.validarInicioFin(inicioFinCliente.cargue, inicioFinCliente.codCliente);
		
		if(id.equals("")){
			
			String validar = DataBaseBO.validarInicioFinClientePendiente(cargue);
			
			if(validar.equals("")){
				boolean guardo = DataBaseBO.GuardarInicioFinCliente(inicioFinCliente);
				
				if(guardo){
					((Button) findViewById(R.id.btnLlegada)).setVisibility(View.GONE);
					((Button) findViewById(R.id.btnSalida)).setVisibility(View.VISIBLE);
					((Button) findViewById(R.id.btnEspera)).setVisibility(View.VISIBLE);
				}else{
					Util.MostrarAlertDialog( this , "No se pudo registrar la informacion" );
				}
			}else{
				Util.MostrarAlertDialog( this , "No ha registrado la salida del cliente: " + validar);
			}
		}else{		
			idTemp = "" + DataBaseBO.validarInicioFinTemp(inicioFinCliente.cargue, inicioFinCliente.codCliente);
			
			if(id.equals("")){
				boolean guardo = DataBaseBO.GuardarInicioFinCliente(inicioFinCliente);
			}
			
			boolean guardo = DataBaseBO.updateInicioFinCliente(id);
				
			if(guardo){
				((Button) findViewById(R.id.btnLlegada)).setVisibility(View.VISIBLE);
				((Button) findViewById(R.id.btnSalida)).setVisibility(View.GONE);
				((Button) findViewById(R.id.btnEspera)).setVisibility(View.GONE);
				((Button) findViewById(R.id.btnFinEspera)).setVisibility(View.GONE);
			}else{
				Util.MostrarAlertDialog( this , "No se pudo registrar la informacion" );
			}	
		}	
	}
	
	@Override
	protected void onResume() {

		super.onResume();
		CargarFacturasPendientes();
		
        String id = "" + DataBaseBO.validarInicioFin(DataBaseBO.getNumeroCargues( Main.usuario.codigoVendedor ), codCliente);
		
		if(id.equals("")){
			((Button) findViewById(R.id.btnLlegada)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.btnSalida)).setVisibility(View.GONE);
		}else{
			((Button) findViewById(R.id.btnLlegada)).setVisibility(View.GONE);
			((Button) findViewById(R.id.btnSalida)).setVisibility(View.VISIBLE);
		}	
	}

	//METODO PARA GUARDAR LAS VARIBLES DE ESPERA EN BASE DE DATOS
	private void marcarEspera() {
		String cargue = DataBaseBO.getNumeroCargues( Main.usuario.codigoVendedor );
		IniciarGPS();

		InicioFinCliente inicioFinCliente = new InicioFinCliente();
		inicioFinCliente.cargue = cargue;
		inicioFinCliente.codCliente = codCliente;
		inicioFinCliente.entregador = Main.usuario.codigoVendedor;
		inicioFinCliente.fechaFinal = "";
		inicioFinCliente.fechaInicial = Util.FechaActual("yyyy-MM-dd HH:mm:ss");
		inicioFinCliente.id = "E" + Util.ObtenerFechaId();

		DataBaseBO.ValidarUsuario();

		String idTemp = "" + DataBaseBO.validarInicioFinTempEspera(inicioFinCliente.cargue, inicioFinCliente.codCliente);

		if (idTemp.equals("")) {
			DataBaseBO.copiaRegistroLlegadaEspera(inicioFinCliente.cargue, inicioFinCliente.codCliente);
		}
		String id = "" + DataBaseBO.validarInicioFinEspera(inicioFinCliente.cargue, inicioFinCliente.codCliente);

		if(id.equals("")){

			String validar = DataBaseBO.validarInicioFinClientePendienteEspera(cargue);

			if(validar.equals("")){
				boolean guardo = DataBaseBO.GuardarInicioFinEspera(inicioFinCliente);

				if(guardo){
					((Button) findViewById(R.id.btnEspera)).setVisibility(View.GONE);
					((Button) findViewById(R.id.btnFinEspera)).setVisibility(View.VISIBLE);
				}else{
					Util.MostrarAlertDialog( this , "No se pudo registrar la informacion" );
				}
			}else{
				Util.MostrarAlertDialog( this , "No ha registrado la salida del cliente: " + validar);
			}

		}else{

			idTemp = "" + DataBaseBO.validarInicioFinTempEspera(inicioFinCliente.cargue, inicioFinCliente.codCliente);

			if(id.equals("")){
				boolean guardo = DataBaseBO.GuardarInicioFinEspera(inicioFinCliente);
			}

			boolean guardo = false;

			///VALIDAR SI EL BOTON DE FINALIZAR ESPERA ESTA VISIBLE PARA AGREGAR LA FECHA FINAL
			if(((Button) findViewById(R.id.btnFinEspera)).getVisibility() ==  View.VISIBLE)
			{
				guardo = DataBaseBO.updateInicioFinEsperaFinal(id);
			}
			else
			{
				guardo = DataBaseBO.updateInicioFinEspera(id);
			}

			if(guardo){
				///VALIDACION PARA OCULTAR LOS BOTONES DE LA ESPERA
				if(((Button) findViewById(R.id.btnFinEspera)).getVisibility() ==  View.VISIBLE)
				{
					((Button) findViewById(R.id.btnEspera)).setVisibility(View.GONE);
					((Button) findViewById(R.id.btnFinEspera)).setVisibility(View.GONE);
				}
				else
				{
					((Button) findViewById(R.id.btnEspera)).setVisibility(View.GONE);
					((Button) findViewById(R.id.btnFinEspera)).setVisibility(View.VISIBLE);
				}
			}else{
				Util.MostrarAlertDialog( this , "No se pudo registrar la informacion" );
			}

		}
	}
}
