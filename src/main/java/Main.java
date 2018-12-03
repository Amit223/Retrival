import ParseObjects.Date;
import ParseObjects.*;
import ParseObjects.Number;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
    Parser p = new Parser();
    StopWords.setStopWords();
    p.Parse("paris Table     of Contents \n" +
            "   FEATURE: \n" +
            "   France: National Debate On Research Policy \n" +
            "   POLICY ISSUES \n" +
            "   Germany Launches New Laser Research Program \n" +
            "   French Report on Critical Defense Technologies \n" +
            "   Finnish Support for R&amp;D \n" +
            "   CORPORATE STRATEGIES \n" +
            "   DASA, China Sign Telecommunications Satellite Agreement \n" +
            "   Swedish Research on Hybrid Vehicles \n" +
            "   Honda Reacts to Sale of Rover \n" +
            "   SPECIAL PUBLICATIONS \n" +
            "   FRANCE: NATIONAL DEBATE ON RESEARCH POLICY \n" +
            "   In recent statements to the press Minister of Higher Education \n" +
            "and Research Francois Fillon confirmed that the French Government \n" +
            "intends to make research a national priority and that it is relying \n" +
            "heavily on major debates now taking place to define the broad \n" +
            "outlines of a national policy by June 1994. \n" +
            "   The national debate on research policy officially got under way \n" +
            "on 1 February, when Robert Daulray of the Atomic Energy Commission \n" +
            "(CEA) presented to Fillon the \"Report on the Trends in Major Aims of \n" +
            "French Research.\"  The 60-page document, which the research minister \n" +
            "commissioned last September, will be circulated to about 30,000 \n" +
            "members of the research community, who will use it as the basis for \n" +
            "a  major national consultation. \"  The consultation exercise will be \n" +
            "based on six thematic conferences to be held in different regions of \n" +
            "the country through mid-March.  The results of these conferences \n" +
            "will be included in a national debate in Paris on 9 April, to be \n" +
            "followed by the drafting in May of a final summary document, which \n" +
            "will serve as the framework for parliamentary discussions on the \n" +
            "future of French research in June (LE FIGARO).  The conferences will \n" +
            "address topics such as research and innovation in small and medium- \n" +
            "sized enterprises, competitiveness-oriented research and the private \n" +
            "sector, basic research, advanced education and research \n" +
            "organizations, and international research (AFS SCIENCES). \n" +
            "   Fillon told LE FIGARO on 2 February that he considers a thorough \n" +
            "assessment of the French scientific community indispensable   This \n" +
            "has not been done for 10 years despite the major changes that have \n" +
            "occurred during that time.  Fillon considers it important for France \n" +
            "to have a more accurate perception of its scientific resources and \n" +
            "of the steps it needs to take during the next few years to remain a \n" +
            "major player in the global S&amp;T arena. \n" +
            "   According to Fillon, French research policy over the past five \n" +
            "years has failed to define the kind of strong priorities that would \n" +
            "encourage \"pump-priming\" programs to stimulate research in a changed \n" +
            "national and international context or that would launch basic \n" +
            "research programs today to lay the groundwork for the applied \n" +
            "research companies will need tomorrow.  AFP SCIENCES has quoted \n" +
            "Fillon as saying that France is the only European country whose \n" +
            "research budget is increasing, but Fillon asserted in LE FIGARO \n" +
            "that, having relinquished much of its leadership role to become \n" +
            "merely a source of funding, the French' Government now needs to find \n" +
            "a way to target research instead of being satisfied to finance it \n" +
            "without having any authority over it.  The government needs to be \n" +
            "able to make selective decisions, such as supporting a particular \n" +
            "laboratory that has achieved a major breakthrough in a field \n" +
            "important to France, Fillon said.  He hopes the current national \n" +
            "debate will result in a policy that will allow researchers the \n" +
            "flexibility to assess needs accurately and allocate efforts \n" +
            "appropriately.  Fillon told AFP SCIENCES he supports proposal5 for a \n" +
            "five-year plan or a programming law like that of the Ministry of \n" +
            "Defense. (AFP SCIENCES 6 Jan 94; LE FIGARO 2 Feb 94) SW \n" +
            "   POLICY ISSUES \n" +
            "   Germany Launches New Laser Research Program--According to \n" +
            "FRANKFURTER ALLGEMEINE, the German Ministry of Research is launching \n" +
            "the \"Laser 2000\" program to promote the development of semiconductor \n" +
            "lasers.  A total of 270 million marks ($159 million) is to be made \n" +
            "available by 1997.  The program is open to small and medium-sized \n" +
            "companies and includes the newly created laser institutes in Berlin- \n" +
            "Adlershof and Dresden, as well as existing research institutes in \n" +
            "Dresden, Jena, Halle, and Rostock. Paris \n" +
            "   The program is Particularly aimed at aiding the transition from \n" +
            "tube lasers to modern Semiconductor lasers.  Germany is two years \n" +
            "behind Japan and the United States in building more powerful \n" +
            "Semiconductor lasers.  For example, one can already buy 100 watt \n" +
            "diode lasers in the United States, whereas in Germany only \n" +
            "laboratory samples exist.  Applications are seen especially in \n" +
            "medicine, for destroying kidney stones and gallstones, or in the \n" +
            "diagnosis and treatment of tumors.  (Frankfurt/Main FRANKFURTER \n" +
            "ALLGEMEINE in German 26 Jan 94) BC \n" +
            "   French Report on Critical Defense Technologies--Faced with \n" +
            "shrinking markets and major job cuts in the defense sector, the \n" +
            "French Government has published a report on \"the future of defenses \n" +
            "related industries\" that prioritizes 24 technologies considered \n" +
            "critical, according to ELECTRONIQUE INTERNATIONAL HEBDO.  The report \n" +
            "states that France will not be able to remain Self-Sufficient in all \n" +
            "areas of equipmens development and will have to enter into \n" +
            "cooperative agreements with its European partners but it \n" +
            "specifically recommends that funding for basic defense research be \n" +
            "maintained at present levels or even increased. The report also \n" +
            "suggests that a special budget be created for evaluation and \n" +
            "acquisition of foreign technologies \n" +
            "   The 24 technologies identified in the report are divided into \n" +
            "three categories: those that need to be totally independent at a \n" +
            "French or European level and whose Proliferation would constitute a \n" +
            "risk, those that have intermediate Priority, and those whose \n" +
            "specifically dual-use nature makes them \"less sensitive.\"  The first \n" +
            "category includes nuclear technology, microelectronic and \n" +
            "nanoelectronic components for equipment and Subassemblies, \n" +
            "navigation equipment, Sensors and signal Processing equipment, \n" +
            "Signature-recogniion expertise, and Stealth-related technologies \n" +
            "The second category consists of technologie5 such as modular \n" +
            "electronic architectures, optical and optronic devices, \n" +
            "telecommunications hardware and networks, advanced computer \n" +
            "hardware, artificial intelligence and neural networks, sof tware \n" +
            "engineering, and advanced computational codes.  The technologies \n" +
            "identified as dual-use include production software and industrial \n" +
            "production systems as well as hardware and software used for \n" +
            "security of equipment and weapons systems. (ELECTRONIQUE \n" +
            "INTERNATIONAL HEBDO 13 Jan 94) SW \n" +
            "   Finnish Support for R&amp;D--TEKES, the Technology Development \n" +
            "Center,  distributed 1.4 billion markkas ($254 million) in 1993 to \n" +
            "firms for 108 research and development projects.  The total sum in \n" +
            "grants and loans was the largest amount ever given in one year. \n" +
            "TEKES, which operates with government funding, distributed more than \n" +
            "half of the grants to small and medium-sized companies.  Projects \n" +
            "receiving the most support were those dealing with information \n" +
            "technology, process technology, manufacturing automation, \n" +
            "construction and Space technology.  Many of the technological \n" +
            "development projects dealt Specifically with improving the \n" +
            "environment. (Helsinki HELSINGIN SANOMAT 10 Feb 94) RB \n" +
            "   CORPORATE STRATEGIES \n" +
            "   DASA, China Sign Telecommunications Satellite Agreement--In \n" +
            "November 1993, Germany's Deutsche Aerospace (DASA) signed a \n" +
            "cooperation treaty with the China Aerospace Corporation (CASC) for \n" +
            "the development, manufacture, and sale of telecommunications, \n" +
            "meteorological, and earth-observation Satellites and ground \n" +
            "stations.  The agreement calls for development of an estimated 20 \n" +
            "telecommunications Satellites over 8 to 10 years for a total worth \n" +
            "of about 1 billion marks ($580 million).  Initial projects will \n" +
            "include: (1) development of new modules for China's DFH-3 \n" +
            "telecommunications satellite (which DASA helped build as part of a \n" +
            "previous agreement), scheduled for launch in mid-1994, and (2) \n" +
            "design of a new-generation telecommunications Satellite (DFH-4) \n" +
            "during 1994 for launch in 1997.  In addition, a jointly owned (50-50 \n" +
            "percent) company with capital assets of DM 20 million ($11.6 \n" +
            "million) will be established, with headquarters in Munich and a \n" +
            "liaison office in Peking.  Finally, a study will examine the \n" +
            "feasibility of setting up a joint Satellite-operations service \n" +
            "agency.  The DASA/China treaty will be in direct competition with \n" +
            "the Alliance--the industrial association formed in 1992 by DASA, \n" +
            "France's Aerospatiale and Alcatel Espace, and Italy's Alenia Spazio \n" +
            "for the construction of civil and military Satellites, in particular \n" +
            "telecommunications Satellites--and may deal it a fatal blow, since \n" +
            "China can manufacture satellites and launchers at prices lower than \n" +
            "those of its European competitors. (Paris AIR &amp; COSMOS/AVIATION \n" +
            "INTERNATIONAL 22/28 Nov 93) AM \n" +
            "   Swedish Research on Hybrid Vehicles--The Swedish Government \n" +
            "intends to spend a total of 120 million kronor ($15 million) on \n" +
            "research concerning electric and hybrid (electric and gas powered) \n" +
            "vehicles.  The project will last until 1997 and cover more than 120 \n" +
            "vehicles.  Research will be concentrated on  Paris hybrid vehicles. \n" +
            "(Stockholm DAGENS NYHETER 11 Jan 94) RB \n"  +
            "   Honda Reacts to Sale of Rover--On 31 January, British Aerospace \n" +
            "announced the sale of Rover to BMW for close to $1.2 billion, \n" +
            "although Honda, which held a 20 percent share of Rover, was \n" +
            "considered to have been the most likely Purchaser.  After the failed \n" +
            "Renault-Volvo merger, the deal marked a new stage in the effort to \n" +
            "restructure the European automobile industry, according to LE MONDE. \n" +
            "   The Honda-Rover alliance was concluded in 1979.  The Japanese \n" +
            "side Specifically aimed at using this relationship to get Honda \n" +
            "automobiles into the European markets. Subsequently, most Rover \n" +
            "models became Simply \"Europeanized\" Hondas, while the British \n" +
            "company acquired 20 percent capital of Honda U.K., the Japanese \n" +
            "company's manufacturing subsidiary in Great Britain. The Honda CEO, \n" +
            "who was informed about the deal only on January 28, expressed his \n" +
            "disappointment, saying that this alliance \"will negate both Honda's \n" +
            "and Rover's efforts to provide a solid future for Rover as a \n" +
            "British--and independent--company\", according to LE MONDE.  By \n" +
            "affirming that the deal with BMW should not adversely affect Honda's \n" +
            "policies in Europe, Mr. Kawamoto, in fact, expressed his \n" +
            "reservations on the future of his company's collaboration with \n" +
            "Rover, according to LE MONDE. Honda apparently intends to continue \n" +
            "its participati0n in the British company unchanged, with the goal of \n" +
            "establishing a viable foothold in Europe. However, the attitude of \n" +
            "BMW may well determine either the Success or failure of the Japanese \n" +
            "plans. (Paris LE MONDE 2 Feb 94) IM \n" +
            "   SPECIAL PUBLICATIONS \n" +
            "   Copies of the annual reports listed below are available upon \n" +
            "request for a period of six months from the date of this Foreign \n" +
            "Media Note from  Cathy Grant at (703) 482-4182. \n" +
            "   *Aerospatiale 1992 annual report, 90 pages, in English. \n" +
            "   *ASEA Brown Boveri 1992 annual report, 68 pages in English. \n" +
            "   *Bofors 1992 annual report, 44 pages, in English. \n" +
            "   *BT 1992/93 annual review, 28 pages, in English. \n" +
            "   *Bull 1992 annual report, 40 pages, in French. \n" +
            "   *Carl Zeiss 1991/92 annual report, 87 pages, in English. \n" +
            "   *CASA 1992 annual report, 51 pages, in English. \n" +
            "   *CEA 1992 annual report, 72 pages, in French. \n" +
            "   *Daimler-Benz 1992 annual report, 90 pages, in English. \n" +
            "   *Dassault Aviation 1992 annual report, 67 pages, in French. \n" +
            "   *Delft Instruments 1992 annual report, 46 pages, in Dutch. \n" +
            "   *Dornier (Deutsche Aerospace) 1992 annual report, 59 pages, \n" +
            "in English. \n" +
            "   *Ericsson 1992 annual report, 65 pages, in English. \n" +
            "   *IFREMER 1992 annual report, 80 pages, in English. \n" +
            "   *Incentive 1992 annual report, 80 pages, in English. \n" +
            "   *Intertechnique 1992 annual report, 85 pages, in French. \n" +
            "   *Kockums Group 1992 annual report, 23 pages, in English. \n" +
            "   *Olivetti Group 1992 consolidated financial Statements, \n" +
            "110 pages, in English. \n" +
            "   *Philips 1992 annual report, 76 pages, in English. \n" +
            "   *Rheinmetall 1992 annual report, 78 pages, in English. \n" +
            "   *S.A.B.C.A. 1992 annual report, 24 pages, in French and Dutch. \n" +
            "   *SAT (Societe Anonyme de Telecommuncations) 1992 annual report, \n" +
            "in French. \n" +
            "   *Simrad Optronics 1992 annual report, in Norwegian/Engli5h. \n" +
            "   *SNECMA 1992 annual report, 71 pages, in French. \n" +
            "   *SNPE 1992 annual report, 49 pages, in French. \n" +
            "   *Teldix GmbH, 1992 annual report, 11 pages, in English. \n" +
            "   *Volvo 1992 annual report, 69 pages, in English. \n" +
            "   (AUTHOR:  COX.  QUESTIONS AND/OR COMMENTS, PLEASE CALL CHIEF, \n" +
            "EUROPE BRANCH AT (703) 733-6337) \n" +
            "GIG/22MAR/ECONF/TID  cka 23/0141z  mar \n",false, "Paris");
    p.printTermList();

    }

}