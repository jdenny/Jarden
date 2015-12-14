package jarden.spanish

object VerbUtils {

}

/*
# -*- coding: latin-1 -*-
'''
Created on 19 Aug 2011
*************At the moment, we never ask for 'YOU'*******************
@author: john
latin-1 coding enables us to put Spanish characters in strings

accentA = u'\xe1'
accentE = u'\xe9'
accentI = u'\xed'
accentO = u'\xf3'
accentU = u'\xf9'
tildeN = u'\xf1' 
'''
from spanish.models import EngSpa

class Tense(object):
    def __init__(self, code, name, diffPersons):
        self.code = code
        self.name = name
        # if false, the conjugation is the same for all persons
        self.diffPersons = diffPersons

_present = Tense('p', 'present', True)
_future = Tense('f', 'future', True)
_preterite = Tense('r', 'preterite', True)
_imperfect = Tense('i', 'imperfect', True)
_imperative = Tense('si', 'imperative', False)
_noImperative = Tense('no', 'no imperative', False)

tenses = (_present, _future, _preterite, _imperfect, _imperative, _noImperative)

class Person(object):
    def __init__(self, code, spaName, engCode, engName):
        self.code = code
        self.spaName = spaName
        self.engCode = engCode
        self.engName = engName

_fs = Person('yo', 'yo', 'I', 'I')
_ss = Person('tu', u'tú', 'thou', 'thou')
_ts = Person('el', u'él/ella/usted', 'he', 'he/she/it')
_fp = Person('n', 'nosotros', 'we', 'we')
_tp = Person('e', 'ellos/ellas/ustedes', 'they', 'they/YOU')

persons = (_fs, _ss, _ts, _fp, _tp)

futureEndings = {'yo':u'é', 'tu':u'ás', 'el':u'á', 'n':'emos', 'e':u'án'}

erPresentEndings = {'yo':'o', 'tu':'es', 'el':'e', 'n':'emos', 'e':'en'}
erPreteriteEndings = {'yo':u'í', 'tu':'iste', 'el':u'ió', 'n':'imos', 'e':'ieron'}
erImperfectEndings = {'yo':u'ía', 'tu':u'ías', 'el':u'ía', 'n':u'íamos', 'e':u'ían'}
erEndings = {'p':erPresentEndings, 'r':erPreteriteEndings, 'i':erImperfectEndings, 'si':'e', 'no':'as'}

arPresentEndings = {'yo':'o', 'tu':'as', 'el':'a', 'n':'amos', 'e':'an'}
arPreteriteEndings = {'yo':u'é', 'tu':'aste', 'el':u'ó', 'n':'amos', 'e':'aron'}
arImperfectEndings = {'yo':'aba', 'tu':'abas', 'el':'aba', 'n':u'ábamos',  'e':'aban'}
arEndings = {'p':arPresentEndings, 'r':arPreteriteEndings, 'i':arImperfectEndings, 'si':'a', 'no':'es'}

irPresentEndings = {'yo':'o', 'tu':'es', 'el':'e', 'n':'imos', 'e':'en'}
irEndings = {'p':irPresentEndings, 'r':erPreteriteEndings, 'i':erImperfectEndings, 'si':'e', 'no':'as'}

engPresentFixes = {'he':'s', 'she':'s', 'it':'s'} # otherwise ''
bePreterite = {'I':'was', 'he':'was', 'she':'was', 'it':'was'} # otherwise 'were'
bePresent = {'I':'am', 'he':'is', 'she':'is', 'it':'is'} # otherwise 'are'
irregEnglishPreterites = {'awake':'awoke', 'bring':'brought', 'break':'broke',
                          'can':'could', 'choose':'chose',
                          'come':'came', 'do':'did', 'drink':'drank', 'eat':'ate',
                          'forget':'forgot',
                          'give':'gave', 'go':'went', 'have':'had',
                          'hear':'heard', 'hide':'hid',
                          'hold':'held',
                          'know':'knew', 'leave':'left', 'make':'made',
                          'put':'did put',
                          'run':'ran', 'say':'said', 'see':'saw', 'sit':'sat',
                          'sleep':'slept',
                          'speak':'spoke', 'steal':'stole',
                          'swim':'swam', 'tell':'told'}   

def conjugateSpanishVerb(spanishVerb, tenseCode, personCode):
    '''  
    returns form of regular or irregular spanish verb, based on tense and person.
    tense can be p(present), r(preterite), f(future), i(imperfect);
    not done yet: s(present subjunctive), c(conditional)
    person can be yo, tu, el, ella, n(nosotros), e(ellos), ellas - ella and ellas not yet included!
    future is different, in that the only irregularity is with the stem;
    the endings are always the same; irregular future stems:
    decir    dir-    to say
    haber    habr-   there to be [impersonal]; to have [helping verb]
    hacer    har-    to make, do
    poder    podr-   to be able
    poner    pondr-  to put, place, set
    querer   querr-  to want, love
    saber    sabr-   to know [a fact], know how [+ infinitive]
    salir    saldr-  to leave, go out
    tener    tendr-  to have
    valer    valdr-  to be worth
    venir    vendr-  to come
    '''
    irregVerbTenses = irregSpaVerbs.get(spanishVerb)
    if tenseCode == 'f':
        if irregVerbTenses != None:
            irrStem = irregVerbTenses.get('f')
            if irrStem != None:
                return irrStem + futureEndings.get(personCode)
        return spanishVerb + futureEndings.get(personCode)
    # for other tenses...    
    # first see if there is an irregularity defined for this verb, tenseCode and person
    conjugatedVerb = None
    if irregVerbTenses != None:
        verbTense = irregVerbTenses.get(tenseCode)
        if verbTense != None:
            if (isinstance(verbTense, str) or isinstance(verbTense, unicode)):
                conjugatedVerb = verbTense
            else:
                conjugatedVerb = verbTense.get(personCode)
    if conjugatedVerb == None:
        # no irregularity found, so find regular form
        stem = spanishVerb[:-2]
        if spanishVerb.endswith('er'):
            endings = erEndings.get(tenseCode)
        elif spanishVerb.endswith('ir'):
            endings = irEndings.get(tenseCode)
        elif spanishVerb.endswith('ar'):
            endings = arEndings.get(tenseCode)
        elif spanishVerb.endswith(u'ír'):
            endings = irEndings.get(tenseCode)
            temp = endings.get(personCode)
            if temp.startswith(u'i'):
                endings = u'í' + temp[1:]
        if isinstance(endings, str) or isinstance(endings, unicode):
            conjugatedVerb = stem + endings
        else:
            conjugatedVerb = stem + endings.get(personCode)
    if tenseCode == 'no':
        prefix = 'no '
    else:
        prefix = ''
    return prefix + conjugatedVerb

alcanzarPreterite = {'yo':u'alcancé'}
alcanzarTenses = {'r':alcanzarPreterite}

darPresent = {'yo':'doy'}
darPreterite = {'yo':'di', 'tu':'diste', 'el':'dio', 'n':'dimos', 'e':'dieron'}
darTenses = {'p':darPresent, 'r':darPreterite}

decirPresent = {'yo':'digo', 'tu':'dices', 'el':'dice', 'n':'decimos', 'e':'dicen'}
decirPreterite = {'yo':'dije', 'tu':'dijiste', 'el':'dijo', 'n':'dijimos', 'e':'dijeron'}
decirTenses = {'p':decirPresent, 'r':decirPreterite, 'f':'dir', 'si':'di', 'no':'digas'}

despertarPresent = {'yo':'despierto', 'tu':'despiertas', 'el':'despierta', 'e':'despiertan'}
despertarTenses = {'p':despertarPresent, 'si':'despiertas', 'no':'despiertes'}

dormirPresent = {'yo':'duermo', 'tu':'duermes', 'el':'duerme', 'n':'dormimos', 'e':'duermen'}
dormirPreterite = {'el':u'durmió', 'e':'durmieron'}
dormirTenses = {'p':dormirPresent, 'r':dormirPreterite, 'si':'duerme', 'no':'duermas'}

estarPresent = {'yo':'estoy', 'tu':u'estás', 'el':u'está', 'n':'estamos', 'e':u'están'}
estarPreterite = {'yo':'estuve', 'tu':'estuviste', 'el':'estuvo', 'n':'estuvimos', 'e':'estuvieron'}
estarTenses = {'p':estarPresent, 'r':estarPreterite, 'si':u'está', 'no':u'estés'}

haberPresent = {'yo':'he', 'tu':'has', 'el':'ha', 'n':'hemos', 'e':'han'}
haberPreterite = {'yo':'hube', 'tu':'hubiste', 'el':'hubo', 'n':'hubimos', 'e':'hubieron'}
haberTenses = {'p':haberPresent, 'r':haberPreterite, 'f':'habr', 'no':'hayas'}

hacerPresent = {'yo':'hago'}
hacerPreterite = {'yo':'hice', 'tu':'hiciste', 'el':'hizo', 'n':'hicimos', 'e':'hicieron'}
hacerTenses = {'p':hacerPresent, 'r':hacerPreterite, 'f':'har', 'si':'haz', 'no':'hagas'}

irPresent = {'yo':'voy', 'tu':'vas', 'el':'va', 'n':'vamos', 'e':'ven'}
irPreterite = {'yo':'fui', 'tu':'fuiste', 'el':'fue', 'n':'fuimos', 'e':'fueron'}
irImperfect = {'yo':'iba', 'tu':'ibas', 'el':'iba', 'n':u'íbamos',  'e':'iban'}
irTenses = {'p':irPresent, 'r':irPreterite, 'i':irImperfect, 'si':'ve', 'no':'vayas'}

jugarPresent = {'yo':'juego', 'tu':'juegas', 'el':'juega', 'n':'jugamos', 'e':'juegan'}
jugarPreterite = {'yo':u'jugué'}
jugarTenses = {'p':jugarPresent, 'r':jugarPreterite, 'si':'juega', 'no':'juegues'}

lloverPresent = {'yo':'lluevo', 'tu':'llueves', 'el':'llueve', 'e':'llueven'}
lloverTenses = {'p':lloverPresent, 'si':'llueve', 'no':'lluevas'}

oirPresent = {'yo':'oigo', 'tu':'oyes', 'el':'oye', 'e':'oyen'}
oirPreterite = {'el':u'oyó', 'e':'oyeron'}
oirTenses = {'p':oirPresent, 'r':oirPreterite, 'f':'oir', 'si':'oye', 'no':'oigas'}

padecerPresent = {'yo':'padezco'}
padecerTenses = {'p':padecerPresent, 'no':'padezcas'}

pensarPresent = {'yo':'pienso', 'tu':'piensas', 'el':'piensa', 'e':'piensan'}
pensarTenses = {'p':pensarPresent, 'si':'piensa', 'no':'pienses', 'si':'piensa', 'no':'pienses'}

poderPresent = {'yo':'puedo', 'tu':'puedes', 'el':'puede', 'e':'pueden'}
poderPreterite = {'yo':'pude', 'tu':'pudiste', 'el':'pudo', 'n':'pudimos', 'e':'pudieron'}
poderTenses = {'p':poderPresent, 'r':poderPreterite, 'f':'podr', 'si':'puede', 'no':'puedas'}

ponerPresent = {'yo':'pongo'}
ponerPreterite = {'yo':'puse', 'tu':'pusiste', 'el':'puso', 'n':'pusimos', 'e':'pusieron'}
ponerTenses = {'p':ponerPresent, 'r':ponerPreterite, 'f':'pondr', 'si':'pon', 'no':'pongas'}

quebrarPresent = {'yo':'quiebro', 'tu':'quiebras', 'el':'quiebra', 'e':'quiebran'}
quebrarTenses = {'p':quebrarPresent, 'si':'quiebra', 'no':'quiebres'}

quererPresent = {'yo':'quiero', 'tu':'quieres', 'el':'quiere','e':'quieren'}
quererPreterite = {'yo':'quise', 'tu':'quisiste', 'el':'quiso', 'n':'quisimos', 'e':'quisieron'}
quererTenses = {'p':quererPresent, 'r':quererPreterite, 'f':'querr', 'si':'quiere', 'no':'quieras'}

reirPresent = {'yo':u'río', 'tu':u'ríes', 'el':u'ríe', 'e':u'ríen'}
reirPreterite = {'yo':u'reí', 'el':u'rió', 'e':'rieron'}
reirTenses = {'p':reirPresent, 'r':reirPreterite, 'f':'reir', 'si':u'ríe', 'no':u'rías'}

saberPresent = {'yo':u'sé', 'tu':'sabes', 'el':'sabe', 'n':'sabemos', 'e':'saben'}
saberPreterite = {'yo':'supe', 'tu':'supiste', 'el':'supo', 'n':'supimos', 'e':'supieron'}
saberTenses = {'p':saberPresent, 'r':saberPreterite, 'f':'sabr', 'no':'sepas'}

salirPresent = {'yo':'salgo'}
salirTenses = {'p':salirPresent, 'f':'saldr', 'si':'sal', 'no':'salgas'}

seguirPresent = {'yo':'sigo', 'tu':'sigues', 'el':'sigue', 'n':'seguimos', 'e':'siguen'}
seguirPreterite = {'el':u'siguió', 'e':'siguieron'}
seguirTenses = {'p':seguirPresent, 'r':seguirPreterite, 'si':'sigue', 'no':'sigas'}

sentarPresent = {'yo':'siento', 'tu':'sientas', 'el':'sienta', 'e':'sientan'}
sentarTenses = {'p':sentarPresent, 'si':'sienta', 'no':'sientes'}

serPresent = {'yo':'soy', 'tu':'eres', 'el':'es', 'n':'somos', 'e':'son'}
serPreterite = {'yo':'fui', 'tu':'fuiste', 'el':'fue', 'n':'fuimos', 'e':'fueron'}
serImperfect = {'yo':'era', 'tu':'eras', 'el':'era', 'n':u'éramos',  'e':'eran'}
serTenses = {'p':serPresent, 'r':serPreterite, 'i':serImperfect, 'si':u'sé', 'no':'seas'}

servirPresent = {'yo':'sirvo', 'tu':'sirves', 'el':'sirve', 'e':'sirven'}
servirPreterite = {'el':u'sirvió', 'e':'sirvieron'}
servirTenses = {'p':servirPresent, 'r':servirPreterite, 'si':'sirve', 'no':'sirvas'}

tenerPresent = {'yo':'tengo', 'tu':'tienes', 'el':'tiene', 'e':'tienen'}
tenerPreterite = {'yo':'tuve', 'tu':'tuviste', 'el':'tuvo', 'n':'tuvimos', 'e':'tuvieron'}
tenerTenses = {'p':tenerPresent, 'r':tenerPreterite, 'f':'tendr', 'si':'ten', 'no':'tengas'}

tocarPreterite = {'yo':u'toqué'}
tocarTenses = {'r':tocarPreterite, 'no':'toques'}

traerPresent = {'yo':'traigo'}
traerPreterite = {'yo':'traje', 'tu':'trajiste', 'el':'trajo', 'n':'trajimos', 'e':'trajeron'}
traerTenses = {'p':traerPresent, 'r':traerPreterite, 'no':'traigas'}

valerPresent = {'yo':'valgo'}
valerTenses = {'p':valerPresent, 'f':'valdr', 'si':'val', 'no':'valgas'}

venirPresent = {'yo':'vengo', 'tu':'vienes', 'el':'viene', 'n':'venimos', 'e':'vienen'}
venirPreterite = {'yo':'vine', 'tu':'viniste', 'el':'vino', 'n':'vinimos', 'e':'vinieron'}
venirTenses = {'p':venirPresent, 'r':venirPreterite, 'f':'vendr', 'si':'ven', 'no':'vengas'}

verPresent = {'yo':'veo'}
verPreterite = {'yo':'vi', 'el':'vio'}
verImperfect = {'yo':u'veía', 'tu':u'veías', 'el':u'veía', 'n':u'veíamos', 'e':u'veían'}
verTenses = {'p':verPresent, 'r':verPreterite, 'i':verImperfect, 'no':'veas'}

irregSpaVerbs = {'alcanzar':alcanzarTenses, 'dar':darTenses,
                 'decir':decirTenses, 'despertar':despertarTenses,
                 'dormir':dormirTenses, 'estar':estarTenses, 'haber':haberTenses,
                 'hacer':hacerTenses, 'ir':irTenses, 'jugar':jugarTenses,
                 'llover':lloverTenses, u'oír':oirTenses,
                 'padecer':padecerTenses, 'pensar':pensarTenses,
                 'poder':poderTenses, 'poner':ponerTenses, 'quebrar':quebrarTenses,
                 'querer':quererTenses, u'reír':reirTenses,
                 'saber':saberTenses, 'salir':salirTenses,
                 'seguir':seguirTenses, 'sentar':sentarTenses,
                 'ser':serTenses, 'servir':servirTenses, 'tener':tenerTenses,
                 'tocar':tocarTenses, 'traer':traerTenses,
                 'valer':valerTenses, 'venir':venirTenses, 'ver':verTenses}

def conjugateEnglishVerb(englishVerb, tenseCode, person):
    '''
    for english verb: person [prefix] verb[suffix]
    present
        I/you[all]/we/they cook
        he/she/it cooks
    imperfect
        I/he/she/it was cooking
        we/they/you/YOU/thou/THOU were cooking
    future
        I/you/he/she/it/we/they will cook
    preterite
        I/you/he/she/it/we/they cooked
    '''
    if (tenseCode == 'p'):
        if (englishVerb == 'be'):
            engVerbMod = bePresent.get(person.engCode)
            if engVerbMod == None: engVerbMod = 'are'
        else:
            suffix = engPresentFixes.get(person.engCode)
            if suffix == None: suffix = ''
            engVerbMod = englishVerb + suffix
    elif (tenseCode == 'i'):
        prefix = bePreterite.get(person.engCode)
        if prefix == None: prefix = 'were'
        suffix = 'ing'
        if (englishVerb.endswith('e') and englishVerb != 'be' and englishVerb != 'see'):
            engVerbMod = prefix + ' ' + englishVerb[0:-1] + suffix
        elif (englishVerb.endswith('et')):
            engVerbMod = prefix + ' ' + englishVerb + 't' + suffix
        else:
            engVerbMod = prefix + ' ' + englishVerb + suffix
    elif (tenseCode == 'f'):
        engVerbMod = 'will ' + englishVerb 
    elif (tenseCode == 'r'):
        if (englishVerb == 'be'):
            engVerbMod = bePreterite.get(person.engCode)
            if engVerbMod == None: engVerbMod = 'were'
        else:
            irregEnglishPreterite = irregEnglishPreterites.get(englishVerb)
            if irregEnglishPreterite == None:
                if (englishVerb.endswith('e')):
                    engVerbMod = englishVerb + 'd'
                else:
                    engVerbMod = englishVerb + 'ed'
            else:
                engVerbMod = irregEnglishPreterite
    elif (tenseCode == 'si'):
        engVerbMod = englishVerb
    elif (tenseCode == 'no'):
        engVerbMod = englishVerb
    return engVerbMod

def getAllConjugations(spanishVerb, englishVerb):
    if englishVerb == '' and spanishVerb:
        englishVerb = EngSpa.objects.filter(spanish=spanishVerb)[0].english
    if spanishVerb == '' and englishVerb:
        spanishVerb = EngSpa.objects.filter(english=englishVerb)[0].spanish
    conjugation = []
    for tense in tenses:
        pairs = []
        if tense.diffPersons:
            for person in persons:
                pairs.append(_getEngSpaConj(spanishVerb, englishVerb, tense, person))
        else:
            pairs.append(_getEngSpaConj(spanishVerb, englishVerb, tense, None))
        conjugation.append((tense.name, pairs))
    return conjugation

def _getEngSpaConj(spanishVerb, englishVerb, tense, person):
    # print 'sp', spanishVerb, 'en', englishVerb, 'tc', tenseCode, 'per', person
    if person:
        spaPerson = person.spaName + ' '
        personCode = person.code
        engPerson = person.engName + ' '
    else:
        spaPerson = ''
        personCode = None
        if (tense.code == 'no'):
            engPerson = "don't "
        else:
            engPerson = ''
    spaVerb = conjugateSpanishVerb(spanishVerb, tense.code, personCode)
    spanish = spaPerson + spaVerb
    english = engPerson + conjugateEnglishVerb(englishVerb, tense.code, person)
    return (spanish, english)

'''
def testVerb(spanishVerb, englishVerb):
    #This method is really superceded by the utility in the web app
    conjugations = getAllConjugations(spanishVerb, englishVerb)
    for tense, pairs in conjugations:
        print "tense=" + tense
        for person, verb in pairs:
            print '  ', person, verb
'''

*/