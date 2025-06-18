--
-- PostgreSQL database dump
--

-- Dumped from database version 16.4
-- Dumped by pg_dump version 16.4

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: activities; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.activities (
    activity_id integer NOT NULL,
    user_id integer NOT NULL,
    activity_date timestamp without time zone NOT NULL,
    description_of_activity text NOT NULL,
    calories_burned numeric(6,2) NOT NULL
);


ALTER TABLE public.activities OWNER TO postgres;

--
-- Name: activities_activity_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.activities_activity_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.activities_activity_id_seq OWNER TO postgres;

--
-- Name: activities_activity_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.activities_activity_id_seq OWNED BY public.activities.activity_id;


--
-- Name: articles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.articles (
    article_id integer NOT NULL,
    title character varying(255) NOT NULL,
    article_text text NOT NULL,
    article_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.articles OWNER TO postgres;

--
-- Name: articles_article_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.articles_article_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.articles_article_id_seq OWNER TO postgres;

--
-- Name: articles_article_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.articles_article_id_seq OWNED BY public.articles.article_id;


--
-- Name: meal_products; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.meal_products (
    meal_product_id integer NOT NULL,
    meal_id integer NOT NULL,
    product_id integer NOT NULL,
    weight_product numeric(6,2) NOT NULL
);


ALTER TABLE public.meal_products OWNER TO postgres;

--
-- Name: meal_products_meal_product_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.meal_products_meal_product_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.meal_products_meal_product_id_seq OWNER TO postgres;

--
-- Name: meal_products_meal_product_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.meal_products_meal_product_id_seq OWNED BY public.meal_products.meal_product_id;


--
-- Name: meals; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.meals (
    meal_id integer NOT NULL,
    user_id integer NOT NULL,
    meal_datetime timestamp without time zone NOT NULL,
    meal_type character varying(20) NOT NULL,
    CONSTRAINT meals_meal_type_check CHECK (((meal_type)::text = ANY ((ARRAY['Завтрак'::character varying, 'Обед'::character varying, 'Перекус'::character varying, 'Ужин'::character varying])::text[])))
);


ALTER TABLE public.meals OWNER TO postgres;

--
-- Name: meals_meal_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.meals_meal_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.meals_meal_id_seq OWNER TO postgres;

--
-- Name: meals_meal_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.meals_meal_id_seq OWNED BY public.meals.meal_id;


--
-- Name: notes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notes (
    note_id integer NOT NULL,
    user_id integer NOT NULL,
    note_text text NOT NULL,
    note_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.notes OWNER TO postgres;

--
-- Name: notes_note_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.notes_note_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.notes_note_id_seq OWNER TO postgres;

--
-- Name: notes_note_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.notes_note_id_seq OWNED BY public.notes.note_id;


--
-- Name: products; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.products (
    product_id integer NOT NULL,
    product_name character varying(100) NOT NULL,
    calories_per_100g numeric(6,2) NOT NULL,
    proteins_per_100g numeric(5,2) NOT NULL,
    fats_per_100g numeric(5,2) NOT NULL,
    carbs_per_100g numeric(5,2) NOT NULL
);


ALTER TABLE public.products OWNER TO postgres;

--
-- Name: products_product_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.products_product_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.products_product_id_seq OWNER TO postgres;

--
-- Name: products_product_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.products_product_id_seq OWNED BY public.products.product_id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    user_id integer NOT NULL,
    first_name character varying(50) NOT NULL,
    last_name character varying(50) NOT NULL,
    birth_date date NOT NULL,
    gender character(1) NOT NULL,
    password character varying(32) NOT NULL,
    height integer NOT NULL,
    weight integer NOT NULL,
    target_weight integer NOT NULL,
    phone character varying(20),
    registration_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT users_gender_check CHECK ((gender = ANY (ARRAY['М'::bpchar, 'Ж'::bpchar])))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_user_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_user_id_seq OWNER TO postgres;

--
-- Name: users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_user_id_seq OWNED BY public.users.user_id;


--
-- Name: waterintake; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.waterintake (
    water_id integer NOT NULL,
    user_id integer NOT NULL,
    intake_date timestamp without time zone NOT NULL,
    amount_liters numeric(4,2) NOT NULL
);


ALTER TABLE public.waterintake OWNER TO postgres;

--
-- Name: waterintake_water_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.waterintake_water_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.waterintake_water_id_seq OWNER TO postgres;

--
-- Name: waterintake_water_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.waterintake_water_id_seq OWNED BY public.waterintake.water_id;


--
-- Name: activities activity_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.activities ALTER COLUMN activity_id SET DEFAULT nextval('public.activities_activity_id_seq'::regclass);


--
-- Name: articles article_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.articles ALTER COLUMN article_id SET DEFAULT nextval('public.articles_article_id_seq'::regclass);


--
-- Name: meal_products meal_product_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.meal_products ALTER COLUMN meal_product_id SET DEFAULT nextval('public.meal_products_meal_product_id_seq'::regclass);


--
-- Name: meals meal_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.meals ALTER COLUMN meal_id SET DEFAULT nextval('public.meals_meal_id_seq'::regclass);


--
-- Name: notes note_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notes ALTER COLUMN note_id SET DEFAULT nextval('public.notes_note_id_seq'::regclass);


--
-- Name: products product_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products ALTER COLUMN product_id SET DEFAULT nextval('public.products_product_id_seq'::regclass);


--
-- Name: users user_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.users_user_id_seq'::regclass);


--
-- Name: waterintake water_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.waterintake ALTER COLUMN water_id SET DEFAULT nextval('public.waterintake_water_id_seq'::regclass);


--
-- Data for Name: activities; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.activities (activity_id, user_id, activity_date, description_of_activity, calories_burned) FROM stdin;
1	2	2025-05-28 00:00:00	Бег	305.00
2	2	2025-05-28 00:00:00	Ходьба	120.00
3	2	2025-05-29 00:00:00	Кросс	215.00
\.


--
-- Data for Name: articles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.articles (article_id, title, article_text, article_date) FROM stdin;
1	How to Start Your Morning Right	A proper morning sets the tone for a productive day. Start with simple habits: wake up at the same time, drink a glass of water, do light exercises or stretching. Avoid checking your phone immediately - instead, spend 10 minutes meditating or journaling. A balanced breakfast with protein, complex carbs, and healthy fats gives you energy and focus. Try planning key tasks for the morning when energy levels are high.	2024-04-01 08:00:00
2	5 Tips for a Productive Day	Productivity isn't about working non-stop - it's about smart energy use. Try these 5 tips: 1) Make a to-do list and highlight priorities. 2) Work in 25-50 minute blocks with short breaks. 3) Eliminate distractions - turn off notifications. 4) Take breaks for lunch and short walks. 5) Review your day in the evening - acknowledging progress boosts motivation. Remember: quality over quantity.	2024-04-02 09:30:00
3	How to Improve Focus in a Distracting World	In today's info-saturated world, focus is crucial. Start by limiting social media time, clearing your workspace, and using the Pomodoro technique: 25 minutes focus, 5 minutes break. Regular exercise and proper sleep greatly enhance concentration. Create a "focus ritual" - light a candle or wear headphones with white noise to signal your brain it's work time.	2024-04-05 07:45:00
4	How to Stay Motivated Long-Term	Motivation comes and goes - actions help sustain it. Set realistic goals and break them down. Keep a progress journal to track successes and challenges. Visualize the end result - a motivating image can help. Find like-minded people - community support matters. When motivation fades, remind yourself why you started.	2024-04-07 10:15:00
5	Healthy Eating Made Easy: Is It Possible?	Healthy eating is not about strict diets - it's about smart habits. Plan meals for the week to avoid impulsive snacking. Cook simple dishes with whole ingredients. Keep healthy snacks nearby - nuts, yogurt, fruit. Stay hydrated. Don't forbid favorite foods - just maintain balance. Over time, good choices become automatic.	2024-04-09 11:00:00
6	The Power of Positive Thinking	Positive thinking is not just a mental state, but a powerful tool that can shape our reality. By training our minds to focus on the good rather than the bad, we unlock the potential to overcome obstacles, achieve goals, and lead a more fulfilling life. When we embrace positivity, we attract opportunities, create better relationships, and enjoy improved mental health. One of the first steps in cultivating positive thinking is to practice gratitude daily. Focus on the little things that bring you joy, and make a habit of acknowledging them. Over time, this shifts your mindset from scarcity to abundance.	2024-05-01 10:00:00
\.


--
-- Data for Name: meal_products; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.meal_products (meal_product_id, meal_id, product_id, weight_product) FROM stdin;
1	3	1	300.00
2	4	2	400.00
3	5	2	500.00
4	6	3	250.00
5	7	3	500.00
6	8	1	400.00
\.


--
-- Data for Name: meals; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.meals (meal_id, user_id, meal_datetime, meal_type) FROM stdin;
3	2	2025-05-28 00:00:00	Завтрак
4	2	2025-05-28 00:00:00	Обед
5	2	2025-05-28 00:00:00	Обед
6	2	2025-05-28 00:00:00	Ужин
7	2	2025-05-29 00:00:00	Обед
8	2	2025-05-27 00:00:00	Завтрак
\.


--
-- Data for Name: notes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.notes (note_id, user_id, note_text, note_date) FROM stdin;
17	2	Сегодня нужно дописать проект по Java	2025-05-28 05:22:02.840601
\.


--
-- Data for Name: products; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.products (product_id, product_name, calories_per_100g, proteins_per_100g, fats_per_100g, carbs_per_100g) FROM stdin;
1	Молоко Вкуснотеево ультрапастеризованное 2.5%	52.50	2.80	2.80	4.70
2	Суп гороховый	33.00	3.70	1.20	1.80
3	Картофельное пюре	81.70	2.10	4.60	8.50
4	Овсянка (варёная)	68.00	2.40	1.40	12.00
5	Яйцо варёное	155.00	13.00	11.00	1.10
6	Тост из цельнозернового хлеба	247.00	13.00	4.20	41.00
7	Греческий йогурт (0% жирности)	59.00	10.00	0.40	3.60
8	Банан	89.00	1.10	0.30	23.00
9	Куриная грудка (варёная)	165.00	31.00	3.60	0.00
10	Рис коричневый (варёный)	123.00	2.60	1.00	25.60
11	Брокколи (варёная)	35.00	2.40	0.40	7.20
12	Лосось (запечённый)	206.00	22.00	13.00	0.00
13	Картофель (запечённый)	93.00	2.50	0.10	21.00
14	Миндаль	579.00	21.00	50.00	22.00
15	Яблоко	52.00	0.30	0.20	14.00
16	Морковь (сырая)	41.00	0.90	0.20	10.00
17	Творог (1% жирности)	72.00	12.00	1.00	3.40
18	Изюм	299.00	3.10	0.50	79.00
19	Индейка (запечённая)	135.00	29.00	1.00	0.00
20	Киноа (варёная)	120.00	4.10	1.90	21.30
21	Шпинат (варёный)	23.00	2.90	0.40	3.60
22	Тунец (консервированный в воде)	116.00	26.00	0.80	0.00
23	Батат (запечённый)	90.00	2.00	0.10	21.00
24	Гречневая каша на воде	132.00	4.50	1.60	25.00
25	Сыр творожный (5% жирности)	120.00	10.00	5.00	3.50
26	Киви	61.00	1.10	0.50	14.70
27	Молоко 2.5%	52.00	2.80	2.50	4.70
28	Мёд	304.00	0.30	0.00	82.40
29	Говядина (тушёная)	215.00	27.00	11.00	0.00
30	Гречка (варёная)	110.00	3.60	1.10	21.30
31	Капуста тушёная	72.00	1.80	3.80	7.40
32	Суп овощной	36.00	1.20	1.50	4.70
33	Макароны из тв. пшеницы (варёные)	124.00	4.50	1.10	24.90
34	Финики сушёные	277.00	1.80	0.20	75.00
35	Грецкий орех	654.00	15.00	65.00	14.00
36	Кефир 1%	40.00	3.00	1.00	4.00
37	Черника (свежая)	57.00	0.70	0.30	14.50
38	Хлебцы ржаные	318.00	9.20	1.50	68.00
39	Омлет из 2 яиц на молоке	150.00	11.00	12.00	1.00
40	Фасоль отварная	127.00	8.40	0.50	22.00
41	Треска (запечённая)	105.00	23.00	0.90	0.00
42	Кабачки тушёные	45.00	1.10	2.30	5.20
43	Салат из свежих овощей с маслом	88.00	1.20	7.00	5.00
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (user_id, first_name, last_name, birth_date, gender, password, height, weight, target_weight, phone, registration_date) FROM stdin;
2	Андрей	Панкратов	2004-07-05	М	andrey2312	186	90	88	+79999228445	2025-05-25 21:57:17.523
\.


--
-- Data for Name: waterintake; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.waterintake (water_id, user_id, intake_date, amount_liters) FROM stdin;
1	2	2025-05-28 00:00:00	0.70
2	2	2025-05-28 00:00:00	0.20
3	2	2025-05-28 00:00:00	0.50
4	2	2025-05-28 00:00:00	0.05
5	2	2025-05-28 00:00:00	0.02
6	2	2025-05-28 00:00:00	0.02
7	2	2025-05-29 00:00:00	0.10
8	2	2025-05-27 00:00:00	1.10
9	2	2025-05-28 00:00:00	0.30
10	2	2025-05-27 00:00:00	0.30
\.


--
-- Name: activities_activity_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.activities_activity_id_seq', 3, true);


--
-- Name: articles_article_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.articles_article_id_seq', 6, true);


--
-- Name: meal_products_meal_product_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.meal_products_meal_product_id_seq', 6, true);


--
-- Name: meals_meal_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.meals_meal_id_seq', 8, true);


--
-- Name: notes_note_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.notes_note_id_seq', 17, true);


--
-- Name: products_product_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.products_product_id_seq', 43, true);


--
-- Name: users_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_user_id_seq', 2, true);


--
-- Name: waterintake_water_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.waterintake_water_id_seq', 10, true);


--
-- Name: activities activities_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.activities
    ADD CONSTRAINT activities_pkey PRIMARY KEY (activity_id);


--
-- Name: articles articles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.articles
    ADD CONSTRAINT articles_pkey PRIMARY KEY (article_id);


--
-- Name: meal_products meal_products_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.meal_products
    ADD CONSTRAINT meal_products_pkey PRIMARY KEY (meal_product_id);


--
-- Name: meals meals_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.meals
    ADD CONSTRAINT meals_pkey PRIMARY KEY (meal_id);


--
-- Name: notes notes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notes
    ADD CONSTRAINT notes_pkey PRIMARY KEY (note_id);


--
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (product_id);


--
-- Name: products products_product_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_product_name_key UNIQUE (product_name);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- Name: waterintake waterintake_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.waterintake
    ADD CONSTRAINT waterintake_pkey PRIMARY KEY (water_id);


--
-- Name: activities activities_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.activities
    ADD CONSTRAINT activities_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- Name: meal_products meal_products_meal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.meal_products
    ADD CONSTRAINT meal_products_meal_id_fkey FOREIGN KEY (meal_id) REFERENCES public.meals(meal_id);


--
-- Name: meal_products meal_products_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.meal_products
    ADD CONSTRAINT meal_products_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(product_id);


--
-- Name: meals meals_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.meals
    ADD CONSTRAINT meals_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- Name: waterintake waterintake_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.waterintake
    ADD CONSTRAINT waterintake_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- PostgreSQL database dump complete
--

