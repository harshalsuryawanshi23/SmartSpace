
import { type HTMLAttributes } from "react";
export const Card: React.FC<HTMLAttributes<HTMLDivElement>> = (props) => {
  return <div className="bg-white shadow rounded-lg" {...props} />;
};

export const CardHeader: React.FC<HTMLAttributes<HTMLDivElement>> = (props) => {
  return <div className="px-4 py-5 border-b border-gray-200 sm:px-6" {...props} />;
};

export const CardTitle: React.FC<HTMLAttributes<HTMLHeadingElement>> = (props) => {
  return <h3 className="text-lg leading-6 font-medium text-gray-900" {...props} />;
};

export const CardContent: React.FC<HTMLAttributes<HTMLDivElement>> = (props) => {
  return <div className="px-4 py-5 sm:p-6" {...props} />;
};
