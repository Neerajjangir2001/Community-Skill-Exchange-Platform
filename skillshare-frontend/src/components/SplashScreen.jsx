import { motion } from "motion/react";
import { Code, Palette, Languages, Megaphone } from "lucide-react";
import { useEffect, useState } from "react";

export function SplashScreen({ onFinish }) {
    const [showConnections, setShowConnections] = useState(false);

    useEffect(() => {
        const timer = setTimeout(() => setShowConnections(true), 800);
        // Add onFinish callback support if passed, assuming 4s duration like previous
        const finishTimer = setTimeout(() => {
            if (onFinish) onFinish();
        }, 4000);

        return () => {
            clearTimeout(timer);
            clearTimeout(finishTimer);
        };
    }, [onFinish]);

    const skillIcons = [
        {
            Icon: Code,
            initialPosition: { x: -80, y: -80 },
            targetPosition: { x: 80, y: 80 },
            color: "#8B5CF6",
        },
        {
            Icon: Palette,
            initialPosition: { x: 80, y: -80 },
            targetPosition: { x: -80, y: 80 },
            color: "#6366F1",
        },
        {
            Icon: Languages,
            initialPosition: { x: -80, y: 80 },
            targetPosition: { x: 80, y: -80 },
            color: "#3B82F6",
        },
        {
            Icon: Megaphone,
            initialPosition: { x: 80, y: 80 },
            targetPosition: { x: -80, y: -80 },
            color: "#06B6D4",
        },
    ];

    const connectionNodes = [
        { id: 1, x: 30, y: 20 },
        { id: 2, x: 70, y: 35 },
        { id: 3, x: 45, y: 65 },
        { id: 4, x: 25, y: 80 },
    ];

    return (
        <div className="fixed inset-0 z-[100] h-screen w-full overflow-hidden bg-gradient-to-br from-blue-400 via-purple-400 to-purple-500">
            {/* Animated Background Overlay */}
            <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 0.2 }}
                transition={{ duration: 1.5 }}
                className="absolute inset-0 bg-gradient-to-tr from-transparent via-white to-transparent"
            />

            {/* Main Content Container */}
            <div className="relative z-10 flex h-full flex-col items-center justify-center px-6">
                {/* Skill Icons Animation Container */}
                <div className="relative mb-12 h-64 w-64">
                    {/* Connection Lines and Dots */}
                    {showConnections && (
                        <svg
                            className="absolute inset-0 h-full w-full"
                            style={{ transform: "scale(0.8)" }}
                        >
                            {/* Animated Connection Lines */}
                            <motion.line
                                x1="30%"
                                y1="20%"
                                x2="70%"
                                y2="35%"
                                stroke="rgba(255, 255, 255, 0.4)"
                                strokeWidth="2"
                                initial={{ pathLength: 0 }}
                                animate={{ pathLength: 1 }}
                                transition={{ duration: 0.8, delay: 0.5 }}
                            />
                            <motion.line
                                x1="70%"
                                y1="35%"
                                x2="45%"
                                y2="65%"
                                stroke="rgba(255, 255, 255, 0.4)"
                                strokeWidth="2"
                                initial={{ pathLength: 0 }}
                                animate={{ pathLength: 1 }}
                                transition={{ duration: 0.8, delay: 0.7 }}
                            />
                            <motion.line
                                x1="45%"
                                y1="65%"
                                x2="25%"
                                y2="80%"
                                stroke="rgba(255, 255, 255, 0.4)"
                                strokeWidth="2"
                                initial={{ pathLength: 0 }}
                                animate={{ pathLength: 1 }}
                                transition={{ duration: 0.8, delay: 0.9 }}
                            />
                            <motion.line
                                x1="25%"
                                y1="80%"
                                x2="30%"
                                y2="20%"
                                stroke="rgba(255, 255, 255, 0.4)"
                                strokeWidth="2"
                                initial={{ pathLength: 0 }}
                                animate={{ pathLength: 1 }}
                                transition={{ duration: 0.8, delay: 1.1 }}
                            />

                            {/* Connection Nodes (Dots) */}
                            {connectionNodes.map((node) => (
                                <motion.circle
                                    key={node.id}
                                    cx={`${node.x}%`}
                                    cy={`${node.y}%`}
                                    r="6"
                                    fill="white"
                                    initial={{ scale: 0, opacity: 0 }}
                                    animate={{ scale: 1, opacity: 1 }}
                                    transition={{
                                        duration: 0.5,
                                        delay: 0.4 + node.id * 0.2,
                                        ease: "easeOut",
                                    }}
                                    style={{
                                        filter: "drop-shadow(0 4px 8px rgba(0, 0, 0, 0.1))",
                                    }}
                                />
                            ))}
                        </svg>
                    )}

                    {/* Skill Icons with Swap Animation */}
                    {skillIcons.map((skillIcon, index) => {
                        const { Icon, initialPosition, targetPosition, color } = skillIcon;
                        return (
                            <motion.div
                                key={index}
                                className="absolute left-1/2 top-1/2 flex h-16 w-16 items-center justify-center rounded-2xl bg-white shadow-lg"
                                initial={{
                                    x: initialPosition.x,
                                    y: initialPosition.y,
                                    scale: 0,
                                    opacity: 0,
                                }}
                                animate={{
                                    x: [initialPosition.x, initialPosition.x, targetPosition.x],
                                    y: [initialPosition.y, initialPosition.y, targetPosition.y],
                                    scale: [0, 1, 1],
                                    opacity: [0, 1, 1],
                                }}
                                transition={{
                                    duration: 2.5,
                                    times: [0, 0.4, 1],
                                    delay: index * 0.1,
                                    ease: [0.43, 0.13, 0.23, 0.96],
                                }}
                            >
                                <Icon size={32} strokeWidth={2} style={{ color }} />
                            </motion.div>
                        );
                    })}
                </div>

                {/* Logo Reveal */}
                <motion.div
                    initial={{ scale: 0, opacity: 0 }}
                    animate={{ scale: 1, opacity: 1 }}
                    transition={{
                        duration: 0.8,
                        delay: 1.8,
                        ease: [0.34, 1.56, 0.64, 1],
                    }}
                    className="mb-6"
                >
                    <div className="flex items-center justify-center">
                        <div className="rounded-3xl bg-white px-8 py-4 shadow-2xl">
                            <h1 className="text-4xl font-bold tracking-tight text-transparent bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text">
                                SkillSwap
                            </h1>
                        </div>
                    </div>
                </motion.div>

                {/* Tagline */}
                <motion.div
                    initial={{ y: 20, opacity: 0 }}
                    animate={{ y: 0, opacity: 1 }}
                    transition={{
                        duration: 0.8,
                        delay: 2.2,
                        ease: "easeOut",
                    }}
                    className="text-center"
                >
                    <p className="text-lg text-white font-medium tracking-wide drop-shadow-md">
                        Skills Exchange. Grow Together.
                    </p>
                </motion.div>

                {/* Loading Indicator */}
                <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{
                        duration: 0.5,
                        delay: 2.6,
                    }}
                    className="mt-12"
                >
                    <div className="flex space-x-2">
                        {[0, 1, 2].map((index) => (
                            <motion.div
                                key={index}
                                className="h-2 w-2 rounded-full bg-white"
                                animate={{
                                    scale: [1, 1.5, 1],
                                    opacity: [0.5, 1, 0.5],
                                }}
                                transition={{
                                    duration: 1.5,
                                    repeat: Infinity,
                                    delay: index * 0.2,
                                    ease: "easeInOut",
                                }}
                            />
                        ))}
                    </div>
                </motion.div>
            </div>
        </div>
    );
}

// Export default to match previous export style if needed, though named export is used in user code.
// The existing import in App.jsx (implied) might be default. Let's provide default export too.
export default SplashScreen;
