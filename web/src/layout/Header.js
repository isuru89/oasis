import React, { Component } from 'react'
import styled from 'styled-components'
import Avatar from '../components/Avatar'
import profile from '../profile.jpg'

const Wrapper = styled.div`
  width: 100%;
  height: 70px;
  display: flex;
  align-items: center;
`

const Logo = styled.div`
  min-width: 120px;
  background-color: white;
  text-align: center;
  line-height: 70px;
  font-size: 30px;
`

const MyWrapper = styled.div`
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  background-color: #20465E;
`

export default class Header extends Component {
  render() {
    return (
      <Wrapper>
        <Logo>
          OASIS
        </Logo>
        <MyWrapper>
          <Avatar size={32} image={profile} />
        </MyWrapper>
      </Wrapper>
    )
  }
}
