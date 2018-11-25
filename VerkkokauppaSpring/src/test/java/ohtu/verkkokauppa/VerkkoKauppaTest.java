package ohtu.verkkokauppa;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class VerkkoKauppaTest {

    Pankki pankki;
    Viitegeneraattori viite;
    Varasto varasto;
    Kauppa kauppa;

    @Before
    public void setUp() {
        pankki = mock(Pankki.class);
        viite = mock(Viitegeneraattori.class);
        varasto = mock(Varasto.class);
        kauppa = new Kauppa(varasto, pankki, viite);
    }


    @Test
    public void ostoksenPaaytyttyaPankinMetodiaTilisiirtoKutsutaan() {
        // luodaan ensin mock-oliot
        //Pankki pankki = mock(Pankki.class);

        //Viitegeneraattori viite = mock(Viitegeneraattori.class);
        // määritellään että viitegeneraattori palauttaa viitten 42
        when(viite.uusi()).thenReturn(42);

        //Varasto varasto = mock(Varasto.class);
        // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        // sitten testattava kauppa
        Kauppa k = new Kauppa(varasto, pankki, viite);

        // tehdään ostokset
        k.aloitaAsiointi();
        k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
        k.tilimaksu("pekka", "12345");

        // sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
        verify(pankki).tilisiirto(anyString(), anyInt(), anyString(), anyString(), anyInt());
        // toistaiseksi ei välitetty kutsussa käytetyistä parametreista
    }

    @Test
    public void ostoksenPaatyttyaPankinMetodiaTilisiirtoKutsutaanOikeillaParametreilla() {
        when(viite.uusi()).thenReturn(99);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("pekka", "54321");

        verify(pankki).tilisiirto(eq("pekka"), eq(99), eq("54321"), anyString(), eq(10));
    }

    @Test
    public void tiliSiirtoToimiiEriTuotteilla() {
        when(viite.uusi()).thenReturn(15);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.saldo(2)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "leipä", 3));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(2);
        kauppa.tilimaksu("seppo", "666");

        verify(pankki).tilisiirto(eq("seppo"), eq(15), eq("666"), anyString(), eq(8));
    }

    @Test
    public void tiliSiirtoToimiiPuuttuvillaTuotteilla() {
        when(viite.uusi()).thenReturn(15);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.saldo(2)).thenReturn(0);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "leipä", 3));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(2);
        kauppa.tilimaksu("seppo", "666");

        verify(pankki).tilisiirto(eq("seppo"), eq(15), eq("666"), anyString(), eq(5));
    }

    @Test
    public void ostoskoriNollaantuu() {
        when(viite.uusi()).thenReturn(15);
        when(varasto.saldo(1)).thenReturn(10).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);

        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("tommi", "123");

        verify(pankki).tilisiirto(anyString(), anyInt(), anyString(), anyString(), eq(5));
    }

    @Test
    public void uusiViiteUudelleTapahtumalle() {
        when(viite.uusi()).thenReturn(1);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        kauppa.aloitaAsiointi();
        kauppa.tilimaksu("pekka", "111");
        verify(pankki).tilisiirto(anyString(), eq(1), anyString(), anyString(), anyInt());

        when(viite.uusi()).thenReturn(2);
        kauppa.aloitaAsiointi();
        kauppa.tilimaksu("pekka", "111");
        verify(pankki).tilisiirto(anyString(), eq(2), anyString(), anyString(), anyInt());
    }

    @Test
    public void ostoskoristaPoistoToimii() {
        when(viite.uusi()).thenReturn(1);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(1);

        kauppa.poistaKorista(1);
        kauppa.tilimaksu("asd", "wasd");

        verify(pankki).tilisiirto(anyString(), anyInt(), anyString(), anyString(), eq(5));
    }


}
