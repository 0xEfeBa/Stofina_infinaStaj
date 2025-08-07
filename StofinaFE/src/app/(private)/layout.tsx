import PrivateLayout from '@/layouts/privateLayout'
import React, { ReactNode } from 'react'

const layout = ({ children }: { children: ReactNode }) => {
    return (
        <PrivateLayout>
            {children}
        </PrivateLayout>
    )
}

export default layout