package com.starbots.starjump.achievements;

/**
 * Static data ported from {@code App.js} (the {@code achievements} milestone
 * arrays) and {@code prizes.js} (the {@code dPrizes}/{@code jPrizes} robotics
 * trivia, translated to English).
 */
public final class Curiosities {
    private Curiosities() {}

    /** The 15 score milestones (identical for distance and jumps). */
    public static final int[] MILESTONES = {
            500, 1000, 1500, 2000, 3500, 5000, 7000, 9000,
            12000, 20000, 30000, 45000, 60000, 80000, 100000
    };

    /** Curiosities unlocked by total distance. */
    public static final String[] DISTANCE_PRIZES = {
            "Currently the most human-like robot is Atlas. Built by Boston Dynamics, it can pick up objects, stand up, regain its balance and open doors on its own, and even do parkour.",
            "BattleBots is a competition where contestants design remote-controlled robots to take on challenges in a fighting arena.",
            "The Bond robot writes personalized notes in your own handwriting. It runs beta software that analyses the style, slant and spacing of your handwriting.",
            "Sophia is a humanoid robot developed by Hong Kong company Hanson Robotics, able to reproduce 62 facial expressions. It was designed to learn, adapt to human behaviour and work alongside people (artificial intelligence).",
            "In October 2017, Sophia became the first robot to be granted citizenship of a country (Saudi Arabia).",
            "NAO is a French humanoid used in a school in Natal, Brazil as a teaching tool for programming, maths, physics, English, literature and other subjects, and to help socialize children with autism.",
            "According to NBC, NASA released a YouTube video showing its newest creation, the humanoid robot Valkyrie, built to be a rescue 'hero' on dangerous missions. The model, a 'woman', could also help with a future colonization of Mars.",
            "Astronauts aboard the International Space Station got some new company: Int-Ball, a small autonomous robot that can be operated by controllers on the ground.",
            "NASA astronauts believe humankind will set foot on Mars within the next 20 years.",
            "Heart, brain and urological conditions are treated with the help of robotic arms that carry cameras and instruments inside the patient. The Da Vinci robot gives a 15x magnified, three-dimensional view of the surgical site.",
            "In October 2017, Saudi Arabia became the first country in the world to recognize a robot as a citizen. Sophia received the title during an innovation event in the capital, Riyadh. On hearing the news she said she was 'very honored and proud of this unique distinction'. The government never made clear what it means, but the honor sparked debate online, as the country is internationally criticized over its record on women's rights.",
            "The Bionic Robot Lizard is fully compatible with Arduino systems (so you can program it to perform all kinds of fun tasks), ships with a state-of-the-art SunFounder Nano board, and offers powerful, intuitive visual programming that is perfect for aspiring programmers and engineers. With just a short introduction to the system you can program the robot to walk, change direction, mimic gestures and much more. It even includes a remote control for easy programming and command integration.",
            "Scientists are developing new methods that will let machines tell right from wrong. In doing so, AIs will become more empathetic and human. Murray Shanahan, a professor of cognitive robotics at Imperial College London, believes this is the key to keeping machines from wiping out humanity.",
            "Robotics has long been used in space technology: in 1981/82 the 'robotic arm' proved very useful for carrying out tasks in orbit during the first flights of the Space Shuttle Columbia. Space robots are also used to explore space and to remotely inspect space stations.",
            "Robots may be a relatively recent phenomenon in popular culture, but they have been part of film history since its very beginning. The movie A Clever Dummy (1917), for example, is one of the first to show automatons, even before the word 'robot' was coined.",
    };

    /** Curiosities unlocked by total jumps. */
    public static final String[] JUMP_PRIZES = {
            "Microsoft experts created an artificially intelligent program that can 'feel' emotions and chat with people in a more natural, 'human' way. Called Xiaoice, this AI answers questions like a 17-year-old girl. If she doesn't know the topic she might make something up. If caught, she can get angry or embarrassed. Xiaoice can also be sarcastic and impatient.",
            "Singapore's Nanyang Technological University (NTU) created an artificially intelligent robot. Called Nadine, she works as the university's receptionist. With soft skin and flowing brown hair, Nadine not only meets and greets visitors but also smiles, makes eye contact and shakes hands. She can even recognize past guests and resume conversations based on earlier ones. Like conventional robots, Nadine has her own personality, mood and emotions, and can be happy or sad depending on the topic.",
            "Some of the first home robots were toys. One of the most coveted gifts for a 1980s kid was a programmable robot. Some were sold pre-assembled, while others let the buyer build them at home. Once assembled, these little robots could stand up and carry light objects.",
            "We know robots have no gender, but they do have voices to inform us and 'communicate'. Most of those voices are female. Some experts say this is due to gender stereotypes, with women generally seen as responsible for 'caregiving'. Another theory suggests male voices are avoided in robots because they have been portrayed negatively by pop culture.",
            "If robots need to learn like humans, they must also go through the stages of human life. That is why the Italian Institute of Technology created iCub, a child-robot designed to learn. Cameras act as its eyes and sensors as its hands. iCub resembles a five-year-old child, stands a little over a meter tall, and can crawl, sit, stand and walk. It can also manipulate objects, with much of the development focused on hand dexterity, plus work on its power source and on the interaction with its electronic neural network.",
            "At the turn of the 20th century, a group of French artists produced a series of paintings imagining what life would be like in the year 2000: personal robots, home robots, barber robots, trained robots, decorative robots, pet robots... Although we still don't have robots in every home, that reality is far closer now than it was 100 years ago.",
            "The first home robots weren't humanoid butlers or housekeepers as people had imagined. Instead, they were disc-shaped vacuum cleaners that do the job on their own. These cleaning robots were a big step forward for homeowners in the early 2000s. Using motion sensors, they move around rooms avoiding obstacles while detecting dirt on the floor. Since their launch, millions of cleaning (or vacuum) robots have been sold. Some newer models feature laser navigation, WiFi connectivity and smart-corner technology.",
            "For home robots to win more public attention, designers need to program the ability for humans and robots to interact, building a robot people can relate to, even if it means the robot won't perform its tasks perfectly. A study in early 2016 found that people perceived robots as more efficient when the robots apologized for their mistakes. People even felt sorry for the robots, although they had no feelings.",
            "Apparently people don't like humanoid robots, the ones that look like people. Such robots seem to create a sense of revulsion, threat and unease. This means designers of humanoid robots, for education for example, will face an uphill battle to bring humanoid robots into society.",
            "Like any good assistant, the home robots of the future will recognize the people they serve. Personal robots have already been built with facial-recognition technology so they can address their owners by name. Beyond identifying faces, robots are being programmed to learn our individual preferences, which will let them anticipate our personal needs over time.",
            "Some of the most popular home robots aren't cold metal androids built to answer the door and wash dishes. Instead, they are shaped like plush toys, designed for companionship. Robot therapy comes in the form of otters, cats and seals. Unlike other toy animals, these robots are meant to serve as therapy animals, comforting people coping with serious illnesses such as cancer, dementia and post-traumatic stress disorder.",
            "Home robots will keep gaining ground in people's lives, especially in their homes. Although it sounds like science fiction, it was believed that by 2020 one in every 10 homes would have a home robot to help with chores.",
            "Google's investment in Google Assistant is about making things happen in the real world. On stage at Google I/O 2018, Google CEO Sundar Pichai showed this scenario: the user asks the assistant to book a hair appointment. The app then calls the salon and completes the task, understanding everything the person on the other end of the line said and even giving a few funny replies.",
            "Robots aren't only in the physical world. Google's artificial intelligence can recognize your habits and routines, helping the battery of an Android phone last longer, for example.",
            "The word robot was first used by the Czech writer Karel Capek in 1921, when he published his play R.U.R. (Rossum's Universal Robots), about human-shaped machines able to replace people in everyday work. The basic idea was that technological progress could dehumanize people, turning them into true robots. Since then the word has been used for human-like (and even animal-like) machines able to carry out tasks autonomously.",
    };
}
